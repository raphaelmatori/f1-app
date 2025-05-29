import { AngularNodeAppEngine, createNodeRequestHandler, isMainModule, writeResponseToNodeResponse } from '@angular/ssr/node';
import express from 'express';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

// Resolve the `browser` and `server` directories
const currentFilePath = fileURLToPath(import.meta.url);
const distPath = dirname(dirname(currentFilePath));
const browserDistPath = join(distPath, 'browser');

const app = express();

// Serve static files from /browser
app.use(express.static(browserDistPath, {
  maxAge: '1y',
  index: false
}));

// Create the Angular app engine
const angularApp = new AngularNodeAppEngine();

// All regular routes use the Angular engine
app.use('/**', (req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

if (isMainModule(import.meta.url)) {
  const port = process.env['PORT'] || 4200;
  app.listen(port, () => {
    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

// Export the request handler for use with serverless functions
export const reqHandler = createNodeRequestHandler(app);
