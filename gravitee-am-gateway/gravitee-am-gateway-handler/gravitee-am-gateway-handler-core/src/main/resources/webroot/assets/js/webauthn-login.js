'use strict';

const loginForm = document.getElementById('login');
const errorElement = document.getElementById('webauthn-error');

const w = new WebAuthn({
    loginPath: loginForm.action.replace('/webauthn/login', '/webauthn/login/credentials')
});

const displayMessage = message => {
    errorElement.getElementsByClassName('error_description')[0].innerHTML = message;
    errorElement.style.display = 'block';
};

const clearMessage = () => {
    errorElement.style.display = 'none';
};

loginForm.onsubmit = () => {
    w
        .login({
            name: this.username.value
        })
        .then(res => {
            clearMessage();
            // insert value as hidden field and submit the form
            let input = document.createElement("input");
            input.setAttribute("type", "hidden");
            input.setAttribute("name", "assertion");
            input.setAttribute("value", res);
            loginForm.appendChild(input);
            loginForm.submit();
        })
        .catch(err => {
            displayMessage(err instanceof DOMException ? err.message : 'Invalid user');
        });
    return false;
};
