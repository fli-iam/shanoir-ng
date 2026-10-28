import { test, expect } from '@playwright/test';
import { login, logout } from './utils/auth';

const randomString = Math.random().toString().substr(2,10);

test('expert-examination-create-delete', async ({ page }) => {
  // role-expert: study draft creation and validation by admin
  await login(page, 'EXPERT');

  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.getByRole('textbox').first().click();
  await page.getByRole('textbox').first().fill('playwright-test-study-draft-create-' + randomString);
  await page.getByRole('listitem').filter({ hasText: 'Start date' }).getByRole('textbox').fill('2026-10-01');
  await page.getByRole('listitem').filter({ hasText: 'End date' }).getByRole('textbox').fill('2026-10-31');
  // select pseudo profile
  const profileItem = page.getByRole('listitem').filter({ hasText: 'Pseudonymization profile' });
  await profileItem.getByPlaceholder('Select an option...').click();
  const option = page.locator('.list > div').filter({ hasText: 'Profile Neurinfo' });
  await expect(option).toBeVisible();
  await option.click();
  // disable study cards
  await page.getByRole('radio').nth(1).check();
  // add center to study
  await page.getByRole('listitem', { name: 'At least one center is' }).click();
  await page.getByRole('textbox', { name: 'Select a center' }).click();
  await page.getByText('CHU Rennes').click();
  await page.locator('.commands > span:nth-child(3) > .fas').click();
  await expect(page.locator('form')).toContainText('CHU Rennes');
  await page.getByRole('button', { name: 'Save' }).click();
  await page.getByRole('button', { name: 'No' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on study');
  await logout(page);

  // role-admin: validate study draft
  await login(page, 'ADMIN');
  await expect(page.getByText(/Notifications\s*\d+/)).toBeVisible();
  await page.getByText(/Notifications\s*\d+/).click();
  await expect(page.getByText(/Draft\s*Studies\s*\d+/)).toBeVisible();
  await page.getByText(/Draft\s*Studies\s*\d+/).click();
  const studyRow = page
    .locator('form')
    .filter({ hasText: 'User ' + process.env.EXPERT_NAME + ' Study playwright-test-study-draft-create-' + randomString });
  await studyRow.getByRole('button').click();   // opens a dialog
  await page.getByRole('button', { name: 'OK' }).click();
  const dialog = page.getByRole('dialog');
  await logout(page);

  await login(page, 'EXPERT');
  await page.goto('/shanoir-ng/subject/list');

  // role-expert: subject: create
  await expect(page.getByRole('heading')).toContainText('Manage subjects', { timeout: 10000});
  await page.getByRole('button', { name: '+ New' }).click();
  await expect(page.getByRole('heading')).toContainText('Create subject');
  await page.locator('#firstName').click();
  await page.locator('#firstName').fill('playwright-test-subject-create-' + randomString);
  await page.locator('#lastName').click();
  await page.locator('#lastName').fill('playwright-test-subject-create-' + randomString);
  await page.locator('#name').click();
  await page.locator('#name').fill('playwright-test-subject-create-' + randomString);
  await page.locator('input[type="date"]').fill('2026-10-22');
  await page.getByRole('listitem').filter({ hasText: 'Subject type Select an option' }).locator('select-box').click();
  await page.locator('.list > div:nth-child(2)').click();
  await expect(page.getByRole('list').filter({ hasText: 'Subject type Patient' }).getByPlaceholder('Select an option...')).toBeVisible();
  const studyItem = page.getByRole('listitem').filter({ hasText: 'Study *' });
  await studyItem.getByPlaceholder('Select an option...').click();
  const studyOption = page.locator('.list > div').filter({ hasText: 'playwright-test-study-draft-create-' + randomString });
  await expect(studyOption).toBeVisible();
  await studyOption.click();
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on subject');
  await expect(page.getByRole('link', { name: 'playwright-test-subject-create-' + randomString })).toBeVisible();

  await page.goto('/shanoir-ng/examination/list');

  await expect(page.getByRole('heading')).toContainText('Manage examinations');
  await expect(page.locator('tbody')).toContainText('Columns settings', { timeout: 10000});
  await expect(page.locator('tbody')).toContainText('Found :');
  await page.getByRole('button', { name: '+ New' }).click();

  const studyItem2 = page.getByRole('listitem').filter({ hasText: 'Research study' });
  await studyItem2.getByPlaceholder('Select an option...').click();
  const studyOption2 = page.locator('.list > div').filter({ hasText: 'playwright-test-study-draft-create-' + randomString }).first();
  await expect(studyOption2).toBeVisible();
  await studyOption2.click();

  const subjectItem = page.getByRole('listitem').filter({ hasText: 'Subject' });
  await subjectItem.getByPlaceholder('Select an option...').click();
  const subjectOption = page.locator('.list > div').filter({ hasText: 'playwright-test-subject-create-' + randomString}).first();
  await expect(subjectOption).toBeVisible();
  await subjectOption.click();

  const centerItem = page.getByRole('listitem').filter({ hasText: 'Center' });
  await centerItem.getByPlaceholder('Select an option...').click();
  const centerOption = page.locator('.list > div').filter({ hasText: 'CHU Rennes'}).first();
  await expect(centerOption).toBeVisible();
  await centerOption.click();

  await page.locator('input[type="date"]').fill('2026-10-28');
  await page.locator('#comment').click();
  await page.locator('#comment').fill('comment');
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on examination');
  await expect(page.locator('ol')).toContainText('Examination date');
  await expect(page.locator('ol')).toContainText('10/28/2026');
  await page.locator('.selected > .arrow > span > .fas').click();
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();
  await expect(page.locator('side-menu')).toContainText('Delete Examination');

  await logout(page);
});
