FROM node:20-alpine
RUN npm i -g bids-validator@latest
WORKDIR /app
RUN npm i amqplib
COPY worker.js /app/worker.js
CMD ["node", "worker.js"]
