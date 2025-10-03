/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
      "no-extra-boolean-cast": "off",
      "no-var": "off",
      "prefer-const": "off",
      "no-empty-pattern": "off",

      // TypeScript ESLint rules - turn off common errors
      "@typescript-eslint/no-require-imports": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-unused-vars": "off",
      "@typescript-eslint/no-empty-function": "off",
      "@typescript-eslint/no-inferrable-types": "off",
      "@typescript-eslint/no-namespace": "off", // ?
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
      "@angular-eslint/no-output-on-prefix": "off",
      "@angular-eslint/prefer-inject": "off",
      "@angular-eslint/prefer-standalone": "off",
      "@angular-eslint/no-output-native": "off",
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
  }
);
