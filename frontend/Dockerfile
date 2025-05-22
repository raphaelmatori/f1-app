# Stage 1: Build the Angular application
FROM node:20-alpine as builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application
COPY . .

# Build the application
RUN npm run build --configuration production

# Stage 2: Serve the application
FROM node:20-alpine

WORKDIR /app

# Copy built application from builder stage
COPY --from=builder /app/dist/f1-app/browser /app/browser
COPY --from=builder /app/dist/f1-app/server /app/server
COPY --from=builder /app/package*.json ./

# Install production dependencies only
RUN npm install --production

# Expose the port the app runs on
EXPOSE 4000

# Start the application
CMD ["node", "/app/server/server.mjs"]