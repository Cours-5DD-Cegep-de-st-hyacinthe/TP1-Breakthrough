FROM node:18-alpine
WORKDIR /breakthrough_frontend/
COPY package*.json ./
RUN npm install
COPY public/ /breakthrough_frontend/public
COPY src/ /breakthrough_frontend/src
COPY package.json /breakthrough_frontend/
RUN npm run build
EXPOSE 3000
RUN apk update && apk add curl
CMD ["npx", "serve", "-s", "build"]