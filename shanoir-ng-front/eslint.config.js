// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");
const unusedImports = require("eslint-plugin-unused-imports");
const importPlugin = require("eslint-plugin-import");


module.exports = tseslint.config(
  {
    files: ["**/*.ts"],
    plugins: {
      "unused-imports": unusedImports,
      "import": importPlugin,
    },
    settings: {
      "import/resolver": {
        typescript: {
          project: "./tsconfig.json",
        },
      },
    },
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...tseslint.configs.stylistic,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      "@angular-eslint/directive-selector": [
        "error",
        {
          type: "attribute",
          //prefix: "app",
          style: "camelCase",
        },
      ],
      "@angular-eslint/component-selector": [
        "error",
        {
          type: "element",
          //prefix: "app",
          style: "kebab-case",
        },
      ],

      // Core ESLint rules - turn off common errors
      "no-prototype-builtins": "off",
      "no-useless-escape": "off",
      "no-empty": "off",
      "no-extra-boolean-cast": "off",
      "no-var": "off",
      "prefer-const": "off",
      "no-empty-pattern": "off",

      // TypeScript ESLint rules - turn off common errors
      "@typescript-eslint/no-explicit-any": "off", // 
      "@typescript-eslint/no-unused-vars": ["error", { "argsIgnorePattern": "^_" }], // allow unused function args starting with _
      "@typescript-eslint/no-inferrable-types": "off", // unjustified imo
      "@typescript-eslint/no-namespace": "off", // useful for enum methods
      "@typescript-eslint/consistent-generic-constructors": "off", // unjustified imo
      "@typescript-eslint/consistent-type-definitions": "off", // unjustified imo

      // Unused imports plugin
      "unused-imports/no-unused-imports": "error",
      
      // Import plugin
      "import/no-unresolved": "error",
      "import/no-duplicates": "error",
      "import/order": [
        "warn",
        {
          "groups": ["builtin", "external", "internal", "parent", "sibling", "index"],
          "newlines-between": "always"
        }
      ],
      
      // Angular ESLint rules
      "@angular-eslint/prefer-inject": "off", // heavy migration & not sure that is relevant
      "@angular-eslint/prefer-standalone": "off", // should it be done later? Standalone is meant to be the standard. Heavy migration.
    },
  },
  {
    files: ["**/*.html"],
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility,
    ],
    rules: {
      "@angular-eslint/template/eqeqeq": "off",
      "@angular-eslint/template/label-has-associated-control": "off",
      "@angular-eslint/template/click-events-have-key-events": "off",
      "@angular-eslint/template/interactive-supports-focus": "off",
      "@angular-eslint/template/mouse-events-have-key-events": "off",
      "@angular-eslint/template/alt-text": "off"
    },
  },
  {
    // Fix false positives in this file for dev env
    files: ["src/app/shared/side-menu/side-menu.component.ts"],
    rules: {
      "import/no-unresolved": "off"
    }
  },
);
