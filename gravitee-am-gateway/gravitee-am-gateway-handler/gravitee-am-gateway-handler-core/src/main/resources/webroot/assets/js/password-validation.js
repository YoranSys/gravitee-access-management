// this script need passwordSettings to be declared before including this script

//Form elements
const passwordInput = document.getElementById("password");
const firstNameInput = document.getElementById("firstName");
const lastNameInput = document.getElementById("lastName");
const emailInput = document.getElementById("username");
const submitBtn = document.getElementById("submitBtn");

//Validation elements
const length = document.getElementById("minLength");
const number = document.getElementById("includeNumbers");
const specialChar = document.getElementById("includeSpecialChar");
const mixedCase = document.getElementById("mixedCase");
const maxConsecutiveLetters = document.getElementById("maxConsecutiveLetters");
const excludeUserProfileInfoInPassword = document.getElementById("excludeUserProfileInfoInPassword");



/**
 * When the user starts to type something inside the password field
 */
passwordInput.addEventListener('input', function () {
    if (passwordSettings == null) {
        return;
    }
    //validate min length
    let isMinLengthOk = true;
    const minLength = passwordSettings.minLength;
    if (minLength != null) {
        isMinLengthOk = passwordInput.value.length >= minLength;
        validateMessageElement(length, isMinLengthOk);
    }

    //validate include numbers
    let isIncludeNumbersOk = true;
    if (passwordSettings.includeNumbers) {
        const numbersPattern = /[0-9]/g;
        isIncludeNumbersOk = passwordInput.value.match(numbersPattern);
        validateMessageElement(number, isIncludeNumbersOk);
    }

    //validate include special characters
    let isIncludeSpecialCharactersOk = true;
    if (passwordSettings.includeSpecialCharacters) {
        const specialCharPattern = /[^a-zA-Z0-9]/g;
        isIncludeSpecialCharactersOk = passwordInput.value.match(specialCharPattern);
        validateMessageElement(specialChar, isIncludeSpecialCharactersOk);
    }

    //validate letters in mixed case
    let isLettersInMixedCaseOk = true;
    if (passwordSettings.lettersInMixedCase) {
        const upperCharPattern = /[A-Z]/g;
        const lowerCharPattern = /[a-z]/g;
        isLettersInMixedCaseOk = passwordInput.value.match(upperCharPattern) && passwordInput.value.match(lowerCharPattern);
        validateMessageElement(mixedCase, isLettersInMixedCaseOk);
    }

    //validate max consecutive letters
    let isMaxConsecutiveLettersOk = true;
    if (passwordSettings.maxConsecutiveLetters != null && passwordSettings.maxConsecutiveLetters > 0) {
        isMaxConsecutiveLettersOk = !isOverMaxConsecutiveLetters(passwordInput.value, passwordSettings.maxConsecutiveLetters);
        validateMessageElement(maxConsecutiveLetters, isMaxConsecutiveLettersOk);
    }

    //validate user profile in password
    let isExcludeUserProfileInfoInPasswordOk = true;
    if (passwordSettings.excludeUserProfileInfoInPassword && firstNameInput && lastNameInput && emailInput) {
        const lowerPassword = passwordInput.value ? passwordInput.value.toLowerCase() : passwordInput.value;
        isExcludeUserProfileInfoInPasswordOk = (
            (!firstNameInput.value || !lowerPassword.includes(firstNameInput.value.toLowerCase())) &&
            (!lastNameInput.value || !lowerPassword.includes(lastNameInput.value.toLowerCase())) &&
            (!emailInput.value || !lowerPassword.includes(emailInput.value.toLowerCase()))
        )
        validateMessageElement(excludeUserProfileInfoInPassword, isExcludeUserProfileInfoInPasswordOk);
    }
    submitBtn.disabled = !(isMinLengthOk && isIncludeNumbersOk && isIncludeSpecialCharactersOk && isLettersInMixedCaseOk && isMaxConsecutiveLettersOk && isExcludeUserProfileInfoInPasswordOk);
});

/**
 *
 * @param el message element to change style
 * @param isValid true -> valid style, false -> invalid style
 */
function validateMessageElement(el, isValid) {
    if (isValid) {
        el.classList.remove("invalid");
        el.classList.add("valid");
    } else {
        el.classList.remove("valid");
        el.classList.add("invalid");
    }
}

/**
 * Test if any character is repeated consecutively more than the giver max number
 *
 * @param str
 * @param max
 * @returns {boolean}
 * str="aaabb", max=3 -> true
 * str="aaabb", max=2 -> false
 */
function isOverMaxConsecutiveLetters(str, max) {
    const len = str.length;
    for (let i = 0; i < len; i++) {
        let cur_count = 1;
        for (let j = i + 1; j < len; j++) {
            if (str.charAt(i) !== str.charAt(j)) {
                break;
            }
            cur_count++;
        }
        if (cur_count > max) {
            return true;
        }
    }
    return false;
}
