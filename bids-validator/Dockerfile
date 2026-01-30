# ---- stage: cache validator deps
FROM denoland/deno:debian AS deno_cache

# Force Deno to store its cache here
ENV DENO_DIR=/deno

ARG BIDS_VALIDATOR_VERSION=2.2.10

# Pre-fetch into Deno cache
RUN deno cache jsr:@bids/validator@${BIDS_VALIDATOR_VERSION}


# ---- stage: runtime
FROM node:20-bullseye-slim

# Copy deno binary
COPY --from=deno_cache /usr/bin/deno /usr/local/bin/deno

# Copy the populated DENO_DIR
COPY --from=deno_cache /deno /deno
ENV DENO_DIR=/deno

WORKDIR /app
RUN npm i amqplib@0.10.9
COPY worker.js /app/worker.js

ENV BIDS_VALIDATOR_VERSION=2.2.10

CMD ["node", "worker.js"]
