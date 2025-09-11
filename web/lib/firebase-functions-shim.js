// Simple shim so client bundles that import 'firebase-functions' won't break under Turbopack.
// Exports empty placeholders that can be imported safely in client code.
module.exports = {
  https: {
    onRequest: () => {
      throw new Error('firebase-functions onRequest is a server-only API and should not be called in the browser.');
    }
  },
  runWith: () => ({
    // returns a no-op wrapper
    run: (fn) => fn,
  }),
};
