const express = require('express');
const compression = require('compression');
const { APP_BASE_HREF } = require('@angular/common');
const { ngExpressEngine } = require('@nguniversal/express-engine');
const { provideModuleMap } = require('@nguniversal/module-map-ngfactory-loader');

const { AppServerModuleNgFactory, LAZY_MODULE_MAP } = require('./dist/f1-champions-explorer/server/main');

const app = express();
const PORT = process.env.PORT || 4000;
const DIST_FOLDER = process.cwd() + '/dist/f1-champions-explorer/browser';

// Enable compression
app.use(compression());

// Serve static files
app.get('*.*', express.static(DIST_FOLDER, {
  maxAge: '1y'
}));

// Set up the engine
app.engine('html', ngExpressEngine({
  bootstrap: AppServerModuleNgFactory,
  providers: [
    provideModuleMap(LAZY_MODULE_MAP),
    { provide: APP_BASE_HREF, useValue: '/' }
  ]
}));

app.set('view engine', 'html');
app.set('views', DIST_FOLDER);

// Handle all routes
app.get('*', (req, res) => {
  res.render('index', { req });
});

// Start the server
app.listen(PORT, () => {
  console.log(`Node Express server listening on http://localhost:${PORT}`);
}); 