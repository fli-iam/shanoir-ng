// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");

module.exports = tseslint.config(
  {
    files: ["**/*.ts"],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...tseslint.configs.stylistic,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // "@angular-eslint/directive-selector": [
      //   "error",
      //   {
      //     type: "attribute",
      //     prefix: "app",
      //     style: "camelCase",
      //   },
      // ],
      // "@angular-eslint/component-selector": [
      //   "error",
      //   {
      //     type: "element",
      //     prefix: "app",
      //     style: "kebab-case",
      //   },
      // ],

      // Core ESLint rules - turn off common errors
      "no-prototype-builtins": "off",
      "no-useless-escape": "off",
      "no-empty": "off",
      "no-irregular-whitespace": "off",
      "no-self-assign": "off",
      "no-inner-declarations": "off",
      "no-extra-boolean-cast": "off",
      "no-control-regex": "off",
      "no-misleading-character-class": "off",
      "no-var": "off",
      "prefer-const": "off",
      "no-empty-pattern": "off",

      // TypeScript ESLint rules - turn off common errors
      "@typescript-eslint/no-require-imports": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-unused-vars": "off",
      "@typescript-eslint/no-empty-function": "off",
      "@typescript-eslint/no-inferrable-types": "off",
      "@typescript-eslint/ban-types": "off",
      "@typescript-eslint/no-non-null-assertion": "off",
      "@typescript-eslint/no-empty-interface": "off",
      "@typescript-eslint/prefer-const": "off",
      "@typescript-eslint/no-this-alias": "off",
      "@typescript-eslint/no-namespace": "off",
      "@typescript-eslint/no-var-requires": "off",
      "@typescript-eslint/no-unsafe-declaration-merging": "off",
      "@typescript-eslint/no-wrapper-object-types": "off",
      "@typescript-eslint/no-unused-expressions": "off",
      "@typescript-eslint/ban-ts-comment": "off",
      "@typescript-eslint/no-duplicate-enum-values": "off",
      "@typescript-eslint/no-unsafe-function-type": "off",
      "@typescript-eslint/prefer-as-const": "off",
      "@typescript-eslint/consistent-indexed-object-style": "off",
      "@typescript-eslint/array-type": "off",
      "@typescript-eslint/consistent-generic-constructors": "off",
      "@typescript-eslint/consistent-type-definitions": "off",
      "@typescript-eslint/consistent-type-assertions": "off",
      "@typescript-eslint/class-literal-property-style": "off",
      
      // Angular ESLint rules - turn off common errors
      "@angular-eslint/no-empty-lifecycle-method": "off",
      "@angular-eslint/use-lifecycle-interface": "off",
      "@angular-eslint/no-input-rename": "off",
      "@angular-eslint/no-output-rename": "off",
      "@angular-eslint/no-host-metadata-property": "off",
      "@angular-eslint/prefer-on-push-component-change-detection": "off",
      "@angular-eslint/component-class-suffix": "off",
      "@angular-eslint/directive-class-suffix": "off",
      "@angular-eslint/no-conflicting-lifecycle": "off",
      "@angular-eslint/no-output-on-prefix": "off",
      "@angular-eslint/contextual-lifecycle": "off",
      "@angular-eslint/prefer-inject": "off",
      "@angular-eslint/array-type": "off",
      "@angular-eslint/prefer-standalone": "off",
      "@angular-eslint/no-output-native": "off",
    },
  },
  // {
  //   files: ["**/*.html"],
  //   extends: [
  //     ...angular.configs.templateRecommended,
  //     ...angular.configs.templateAccessibility,
  //   ],
  //   rules: {},
  // }
);
