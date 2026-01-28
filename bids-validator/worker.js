/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

const amqp = require("amqplib");
const fs = require("fs");
const { execFile } = require("node:child_process");

const AMQP_URL = process.env.AMQP_URL;
if (!AMQP_URL) {
    console.error("AMQP_URL is missing");
    process.exit(2);
}

const IN_QUEUE = process.env.IN_QUEUE || "bids.validate";
const OUT_QUEUE = process.env.OUT_QUEUE || "bids.validated";
const DATA_ROOT = process.env.DATA_ROOT || "/data";

function runValidator(path) {
    return new Promise((resolve) => {
        execFile(
            "bids-validator",
            ["--json", path],
            { maxBuffer: 50 * 1024 * 1024 },
            (err, stdout, stderr) => {
                const exitCode = err && typeof err.code === "number" ? err.code : 0;

                let report = null;
                try {
                    report = JSON.parse(stdout || "{}");
                } catch {
                    report = { raw: stdout || "" };
                }

                resolve({ exitCode, report, stderr: stderr || "" });
            }
        );
    });
}

async function connectWithRetry() {
    while (true) {
        try {
            return await amqp.connect(AMQP_URL);
        } catch (e) {
            console.error("AMQP connect failed, retrying...", e.message);
            await new Promise((r) => setTimeout(r, 2000));
        }
    }
}

(async () => {
    const conn = await connectWithRetry();
    const ch = await conn.createChannel();

    await ch.assertQueue(IN_QUEUE, { durable: true });
    await ch.assertQueue(OUT_QUEUE, { durable: true });
    ch.prefetch(1);

    ch.consume(
        IN_QUEUE,
        async (msg) => {
            if (!msg) return;

            const input = msg.content.toString("utf8").trim();
            if (!input) {
                ch.nack(msg, false, false);
                return;
            }

            const path = input.startsWith("/") ? input : `${DATA_ROOT}/${input}`;
            console.log("Using path:", path);

            // 1) Listing (debug)
            try {
                const entries = fs.readdirSync(path, { withFileTypes: true });
                console.log(
                    "Directory content:",
                    entries.map((e) => ({
                        name: e.name,
                        type: e.isDirectory() ? "dir" : "file",
                    }))
                );
            } catch (err) {
                const out = { input, path, ok: false, error: `Cannot read directory: ${err.message}` };
                ch.sendToQueue(OUT_QUEUE, Buffer.from(JSON.stringify(out)), {
                    persistent: true,
                    contentType: "application/json",
                    correlationId: msg.properties.correlationId
                });
                ch.ack(msg);
                return;
            }

            // 2) Validation
            try {
                const { exitCode, report, stderr } = await runValidator(path);
                const ok = exitCode === 0;

                const out = { input, path, ok, exitCode, report, stderr };
                ch.sendToQueue(OUT_QUEUE, Buffer.from(JSON.stringify(out)), {
                    persistent: true,
                    contentType: "application/json",
                    correlationId: msg.properties.correlationId
                });

                ch.ack(msg);
            } catch (err) {
                const out = { input, path, ok: false, error: `Validator crash: ${err.message || String(err)}` };
                ch.sendToQueue(OUT_QUEUE, Buffer.from(JSON.stringify(out)), {
                    persistent: true,
                    contentType: "application/json",
                    correlationId: msg.properties.correlationId
                });
                ch.ack(msg);
            }
        },
        { noAck: false }
    );

    console.log(`Worker ready. IN=${IN_QUEUE} OUT=${OUT_QUEUE}`);
})();
