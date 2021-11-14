'use strict';

const registerForm = document.getElementById('register');
const errorElement = document.getElementById('webauthn-error');

const w = new WebAuthn({
    registerPath: registerForm.action.replace('/webauthn/register', '/webauthn/register/credentials')
});

const displayMessage = message => {
    errorElement.getElementsByClassName('error_description')[0].innerHTML = message;
    errorElement.style.display = 'block';
};

const clearMessage = () => {
    errorElement.style.display = 'none';
};

registerForm.onsubmit = () => {
    w
        .register({
            name: this.username.value,
            displayName: this.username.value
        })
        .then(res => {
            clearMessage();
            // insert value as hidden field and submit the form
            let input = document.createElement("input");
            input.setAttribute("type", "hidden");
            input.setAttribute("name", "assertion");
            input.setAttribute("value", res);
            registerForm.appendChild(input);
            registerForm.submit();
        })
        .catch(err => {
            displayMessage(err instanceof DOMException ? err.message : 'Invalid user');
        });
    return false;
};
