package com.automation.ui.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

public final class LoginPage extends BasePage {
    public LoginPage(Page page) {
        super(page);
    }

    public void open(String url) {
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }
}
