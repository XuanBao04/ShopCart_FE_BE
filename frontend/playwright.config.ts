import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './src/tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    ['list'],
  ],
  timeout: 60000,
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on',
    actionTimeout: 15000,
  },

  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        storageState: 'src/tests/e2e/.auth/user.json',
      },
    },

    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        storageState: 'src/tests/e2e/.auth/user.json',
      },
    },

    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        storageState: 'src/tests/e2e/.auth/user.json',
      },
    },
  ],

  globalSetup: "./src/tests/e2e/global-setup.ts",

  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
})
