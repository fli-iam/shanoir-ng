import { Page, expect } from '@playwright/test';
import { authenticator } from 'otplib';

export type Role = 'ADMIN' | 'EXPERT' | 'USER';

function getCredentials(role: Role) {
  const name = process.env[`${role}_NAME`];
  const password = process.env[`${role}_PASSWORD`];
  const otpSecret = process.env[`${role}_OTP_SECRET`];
  if (!name || !password) {
    throw new Error(`Missing ${role}_NAME or ${role}_PASSWORD in the .env file`);
  }
  return { name, password, otpSecret };
}

/**
 * Opens the application and logs in through Keycloak with the given role.
 * If <ROLE>_OTP_SECRET is set in the .env, the one-time password step is handled too.
 */
export async function login(page: Page, role: Role): Promise<void> {
  const { name, password, otpSecret } = getCredentials(role);

  await page.goto(process.env.BASE_URL!);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).fill(name);
  await page.getByRole('textbox', { name: 'Password' }).fill(password);
  await page.getByRole('button', { name: 'Sign In' }).click();

  if (otpSecret) {
    const otp = authenticator.generate(otpSecret);
    await page.fill('#otp-input', otp);
    await page.click('#verify');
  }
}

export async function logout(page: Page): Promise<void> {
  await page.getByText('Logout').click();
}
