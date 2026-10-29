import { test, expect } from '@playwright/test';
import { login, logout } from './utils/auth';

const randomString = Math.random().toString().substr(2,10);

test('expert-subject-examination-create-delete', async ({ page }) => {
  // role-expert: study draft creation and validation by admin
  await login(page, 'EXPERT');

  // center create
  await page.getByText('Medical configuration').click();
  await page.getByText('Acquisition centers').click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.locator('#name').click();
  await page.locator('#name').fill('center-' + randomString);
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on acquisition center', { timeout: 10000});
  await expect(page.locator('ol')).toContainText('center-' + randomString);

  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.getByRole('textbox').first().click();
  await page.getByRole('textbox').first().fill('study-draft-' + randomString);
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
  await page.getByText('center-' + randomString).click();
  await page.locator('.commands > span:nth-child(3) > .fas').click();
  await expect(page.locator('form')).toContainText('center-' + randomString);
  await page.getByRole('button', { name: 'Save' }).click();
  await page.getByRole('button', { name: 'No' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on study', { timeout: 10000});
  await logout(page);

  // role-admin: validate study draft
  await login(page, 'ADMIN');
  await expect(page.getByText(/Notifications\s*\d+/)).toBeVisible();
  await page.getByText(/Notifications\s*\d+/).click();
  await expect(page.getByText(/Draft\s*Studies\s*\d+/)).toBeVisible({ timeout: 10000});
  await page.getByText(/Draft\s*Studies\s*\d+/).click();
  const studyRow = page
    .locator('form')
    .filter({ hasText: 'User ' + process.env.EXPERT_NAME + ' Study study-draft-' + randomString });
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
  await page.locator('#firstName').fill('subject-' + randomString);
  await page.locator('#lastName').click();
  await page.locator('#lastName').fill('subject-' + randomString);
  await page.locator('#name').click();
  await page.locator('#name').fill('subject-' + randomString);
  await page.locator('input[type="date"]').fill('2026-10-22');
  await page.getByRole('listitem').filter({ hasText: 'Subject type Select an option' }).locator('select-box').click();
  await page.locator('.list > div:nth-child(2)').click();
  await expect(page.getByRole('list').filter({ hasText: 'Subject type Patient' }).getByPlaceholder('Select an option...')).toBeVisible();
  
  const studyBox = page.locator('select-box[formcontrolname="study"]');
  await studyBox.locator('input.label').fill('study-draft-' + randomString);
  const studyOption = studyBox.locator('.list .option', { hasText: 'study-draft-' + randomString });
  await expect(studyOption.first()).toBeVisible({ timeout: 15000 });
  await studyOption.first().click();

  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on subject', { timeout: 15000 });
  await expect(page.getByRole('link', { name: 'subject-' + randomString })).toBeVisible();

  await page.goto('/shanoir-ng/examination/list');

  await expect(page.getByRole('heading')).toContainText('Manage examinations');
  await page.getByRole('button', { name: '+ New' }).click();

  const studyBoxExam = page.locator('select-box[formcontrolname="study"]');
  await studyBoxExam.locator('input.label').fill('study-draft-' + randomString);
  const studyOptionExam = studyBoxExam.locator('.list .option', { hasText: 'study-draft-' + randomString });
  await expect(studyOptionExam.first()).toBeVisible({ timeout: 15000 });
  await studyOptionExam.first().click();

  const subjectItem = page.getByRole('listitem').filter({ hasText: 'Subject' });
  await subjectItem.getByPlaceholder('Select an option...').click();
  const subjectOption = page.locator('.list > div').filter({ hasText: 'subject-' + randomString}).first();
  await expect(subjectOption).toBeVisible();
  await subjectOption.click();

  const centerItem = page.getByRole('listitem').filter({ hasText: 'Center' });
  await centerItem.getByPlaceholder('Select an option...').click();
  const centerOption = page.locator('.list > div').filter({ hasText: 'center-' + randomString}).first();
  await expect(centerOption).toBeVisible();
  await centerOption.click();

  await page.locator('input[type="date"]').fill('2026-10-28');
  await page.locator('#comment').click();
  await page.locator('#comment').fill('comment');
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on examination', { timeout: 15000});
  await expect(page.locator('ol')).toContainText('Examination date');
  await expect(page.locator('ol')).toContainText('10/28/2026');
  await page.locator('.selected > .arrow > span > .fas').click();
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();

  await logout(page);
});
