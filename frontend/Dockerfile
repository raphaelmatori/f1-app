# Stage 1: Build the Angular application
FROM node:20-alpine as builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application
COPY . .

# Build the application with production configuration
RUN npm run build -- --configuration production

# Stage 2: Serve the application with nginx
FROM nginx:alpine

# Copy the built application from the builder stage
COPY --from=builder /app/dist/f1-app/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]