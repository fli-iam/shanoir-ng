import { test, expect } from '@playwright/test';

const randomString = Math.random().toString().substr(2,10);

test('test', async ({ page }) => {
  await page.goto(process.env.BASE_URL);

  // role-admin: test study creation
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.ADMIN_NAME);
  await page.getByRole('textbox', { name: 'Username or email' }).press('Tab');
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.ADMIN_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.getByRole('textbox').first().click();
  await page.getByRole('textbox').first().fill('playwright-test-study-create-' + randomString);
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
  // save study
  await page.getByRole('button', { name: 'Save' }).click();
  await page.getByRole('button', { name: 'No' }).click();
  await expect(page.locator('study-node')).toContainText('playwright-test-study-create-' + randomString);
  await expect(page.getByRole('heading')).toContainText('Details on study');
  await page.getByText('Logout').click();

  // role-expert: test study draft creation and validation by admin
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('button', { name: '+ New' }).click();
  await page.getByRole('textbox').first().click();
  await page.getByRole('textbox').first().fill('playwright-test-study-draft-create-' + randomString);
  await page.getByRole('listitem').filter({ hasText: 'Start date' }).getByRole('textbox').fill('2026-10-01');
  await page.getByRole('listitem').filter({ hasText: 'End date' }).getByRole('textbox').fill('2026-10-31');
  // select pseudo profile
  await profileItem.getByPlaceholder('Select an option...').click();
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
  await page.getByText('Logout').click();
  await page.locator('div').nth(2).click();
  // role-admin: validate study draft
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.ADMIN_NAME);
  await page.getByRole('textbox', { name: 'Username or email' }).press('Tab');
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.ADMIN_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
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
  await page.getByText('Logout').click();
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.locator('#kc-page-title')).toContainText('Sign in to your account');
  await page.getByRole('textbox', { name: 'Username or email' }).click();
  await page.getByRole('textbox', { name: 'Username or email' }).fill(process.env.EXPERT_NAME);
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill(process.env.EXPERT_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('textbox', { name: 'search...' }).click();
  await page.getByRole('link', { name: 'playwright-test-study-draft-create-' + randomString }).click();
  await expect(page.getByRole('heading')).toContainText('Details on study');

  // role-expert: add member with can_admin rights
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('link', { name: 'playwright-test-study-draft-create-' + randomString }).click();
  await page.getByRole('button', { name: 'Edit' }).click();
  await page.getByRole('listitem').filter({ hasText: 'Members' }).click();
  await page.getByRole('textbox', { name: 'email address / username' }).click();
  await page.getByRole('textbox', { name: 'email address / username' }).fill(process.env.ADMIN_NAME);
  await page.getByRole('button', { name: 'Invite & Add +' }).click();
  await page.locator('.cell.cell-boolean > .cell-container').first().click();
  await page.locator('.cell.cell-date > .cell-container').first().click();
  await page.locator('li:nth-child(10) > .right-col > .ng-untouched').click();
  await page.getByRole('button', { name: 'Update' }).click();
  await page.locator('.cell.cell-date > .cell-container').first().click();
  await expect(page.getByRole('heading')).toContainText('Details on study');
  await expect(page.locator('studyuser-list')).toContainText(process.env.ADMIN_NAME);
  
  // role-expert: test subject creation and deletion
  await page.locator('side-menu').getByText('Home').click();
  await expect(page.getByText('Subjects')).toBeVisible();
  await page.getByText('Subjects').click();
  await expect(page.getByRole('heading')).toContainText('Manage subjects');
  await page.getByRole('button', { name: '+ New' }).click();
  await expect(page.getByRole('heading')).toContainText('Create subject');
  await page.getByRole('group', { name: 'Study' }).locator('select-box').click();
  await page.getByText('playwright-test-study-draft-create-' + randomString).click();
  await expect(page.getByRole('group', { name: 'Study' }).getByPlaceholder('Select an option...')).toBeVisible();
  await page.getByRole('listitem').filter({ hasText: 'Subject type Select an option' }).locator('select-box').click();
  await page.locator('.list > div:nth-child(2)').click();
  await expect(page.getByRole('list').filter({ hasText: 'Subject type Patient' }).getByPlaceholder('Select an option...')).toBeVisible();
  await page.locator('#firstName').click();
  await page.locator('#firstName').fill('playwright-test-subject-create-' + randomString);
  await page.locator('#lastName').click();
  await page.locator('#lastName').fill('playwright-test-subject-create-' + randomString);
  await page.locator('#name').click();
  await page.locator('#name').fill('playwright-test-subject-create-' + randomString);
  await page.locator('input[type="date"]').fill('2026-10-22');
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByRole('heading')).toContainText('Details on subject');
  await expect(page.getByRole('link', { name: 'playwright-test-subject-create-' + randomString })).toBeVisible();
  await page.getByRole('button', { name: 'Delete' }).click();
  await page.getByRole('button', { name: 'OK' }).click();
  await expect(page.locator('side-menu')).toContainText('Delete Subject');
  
  // role-expert: test add DUA and protocol file 
  await page.getByText('Medical data').click();
  await page.getByText('Studies', { exact: true }).click();
  await page.getByRole('link', { name: 'playwright-test-study-draft-create-' + randomString }).click();
  await page.getByRole('button', { name: 'Edit' }).click();
  await page.getByText('Files', { exact: true }).click();
  await page.getByRole('button', { name: 'Choose file' }).click();
  await page.getByRole('group', { name: 'Data use agreement (DUA)' }).locator('input[type="file"]').setInputFiles('test_pdf.pdf');
  await page.getByRole('button', { name: 'Upload a new file' }).click();
  await page.locator('input[type="file"]').setInputFiles('test_pdf.pdf');
  await page.getByRole('button', { name: 'Update' }).click();
  const downloadPromise = page.waitForEvent('download');
  await page.locator('.right-col > span > .dl-button').click();
  const download = await downloadPromise;
  const download1Promise = page.waitForEvent('download');
  await page.locator('li > .dl-button').first().click();
  const download1 = await download1Promise;
  const download2Promise = page.waitForEvent('download');
  await page.locator('li:nth-child(2) > .dl-button').click();
  const download2 = await download2Promise;
  
  await page.getByText('Logout').click();
});
