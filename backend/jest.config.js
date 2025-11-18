module.exports = {
  testEnvironment: 'node',
  coverageDirectory: 'coverage',
  collectCoverageFrom: [
    'services/**/*.js',
    'middleware/**/*.js',
    'utils.js',
    '!**/node_modules/**'
  ],
  testMatch: [
    '**/test/**/*.test.js'
  ],
  verbose: true
};
