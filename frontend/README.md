# F1 App

A modern Formula 1 statistics application built with Angular.

## Features

- View F1 seasons and champions
- Detailed race winner information
- Responsive design with Tailwind CSS
- Comprehensive test coverage

## Prerequisites

- Node.js 18+
- npm 9+

## Installation

```bash
npm install
```

## Development

```bash
# Start development server
npm start
```

## Testing

### Unit Tests

```bash
# Run unit tests
npm test

# Run with coverage
npm run test:coverage
```

### E2E Tests

```bash
# Run E2E tests in browser
npm run e2e

# Run E2E tests headless
npm run e2e:headless

# Run E2E tests with coverage
npm run e2e:coverage
```

### Run All Tests

```bash
# Run all tests (unit + E2E)
npm run test:all
```

## Dependency Management

```bash
# Check for outdated or vulnerable dependencies
npm run deps:check

# Fix dependency issues
npm run deps:fix
```

## Build

```bash
# Build for production
npm run build -- --configuration production
```

## Docker

```bash
# Build and run with Docker
docker build -t f1-app .
docker run -p 80:80 f1-app
```

## Performance

The application is optimized for performance with:
- Lazy loading
- Code splitting
- Optimized assets
- Caching strategies

## Contributing

1. Fork the repository
2. Create your feature branch
3. Run tests and ensure they pass
4. Submit a pull request

## License

MIT
