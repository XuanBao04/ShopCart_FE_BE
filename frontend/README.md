# Frontend ShopCart

React 19.x frontend application for ShopCart e-commerce platform.

## Tech Stack

- **React 19.x** - UI Framework
- **Vite** - Build tool & Dev server
- **TypeScript** - Type safety
- **TailwindCSS** - Styling
- **Axios** - HTTP client
- **Vitest** - Unit testing
- **React Testing Library** - Component testing
- **Playwright** - End-to-end testing

## Project Structure

```
frontend/
├── src/
│   ├── components/       # Reusable React components
│   │   ├── Cart.tsx
│   │   ├── Checkout.tsx
│   │   ├── Inventory.tsx
│   │   └── index.ts
│   ├── services/         # API service layer
│   │   ├── apiClient.ts
│   │   ├── cartService.ts
│   │   ├── orderService.ts
│   │   ├── productService.ts
│   │   └── index.ts
│   ├── utils/            # Utility functions
│   │   ├── validation.ts
│   │   ├── priceCalculation.ts
│   │   └── index.ts
│   ├── hooks/            # Custom React hooks
│   │   ├── useAsync.ts
│   │   ├── useLocalStorage.ts
│   │   ├── useDebounce.ts
│   │   └── index.ts
│   ├── tests/            # Unit tests
│   │   ├── setup.ts
│   │   ├── Cart.test.tsx
│   │   ├── validation.test.ts
│   │   └── priceCalculation.test.ts
│   ├── App.tsx           # Root component
│   ├── App.css
│   ├── main.tsx
│   ├── index.css
│   └── vite-env.d.ts
├── e2e/                  # End-to-end tests
│   ├── home.spec.ts
│   ├── cart.spec.ts
│   └── checkout.spec.ts
├── public/               # Static assets
├── .env.example          # Environment variables template
├── .eslintrc.json        # ESLint configuration
├── index.html            # HTML entry point
├── package.json          # Dependencies
├── playwright.config.ts  # Playwright configuration
├── postcss.config.js     # PostCSS configuration
├── tailwind.config.js    # TailwindCSS configuration
├── tsconfig.json         # TypeScript configuration
├── tsconfig.node.json    # TypeScript config for Node files
├── vite.config.ts        # Vite configuration
└── vitest.config.ts      # Vitest configuration
```

## Getting Started

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The development server will start at `http://localhost:5173`

### Build

```bash
npm run build
```

### Testing

#### Unit Tests

```bash
npm run test
```

View test UI:
```bash
npm run test:ui
```

Coverage report:
```bash
npm run test:coverage
```

#### End-to-End Tests

```bash
npm run e2e
```

UI mode:
```bash
npm run e2e:ui
```

Debug mode:
```bash
npm run e2e:debug
```

### Linting

```bash
npm run lint
```

## Environment Variables

Copy `.env.example` to `.env` and update values:

```bash
cp .env.example .env
```

Available variables:
- `VITE_API_URL` - Backend API URL (default: http://localhost:8080/api)
- `VITE_APP_NAME` - Application name

## API Integration

The frontend uses **Axios** for HTTP requests with a configured API client in `src/services/apiClient.ts`.

### Services

- **CartService** - Cart management operations
- **OrderService** - Order creation and management
- **ProductService** - Product listing and search

## Components

### Cart
Shopping cart display with add/remove/update quantity functionality.

### Checkout
Order form with validation and submission.

### Inventory
Product listing and management interface.

## Utilities

### Validation
Email, phone, zip code, quantity, price validation functions.

### Price Calculation
Tax calculation, discount calculation, shipping cost, price formatting.

## Custom Hooks

- `useAsync` - Async operation management
- `useLocalStorage` - Local storage integration
- `useDebounce` - Debounce function calls

## Styling

Project uses **TailwindCSS** for styling with **PostCSS** for processing.

Configure in `tailwind.config.js` and `postcss.config.js`.

## Contributing

1. Follow TypeScript and React best practices
2. Add tests for new features
3. Use TailwindCSS classes for styling
4. Keep components small and reusable

## License

MIT
