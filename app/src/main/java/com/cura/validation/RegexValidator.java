/*
 Copyright© 2010, 2011 Ahmad Balaa, Oday Maleh

 This file is part of Cura.

	Cura is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cura is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cura.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cura.validation;

/*
 * Description: The purpose of this class is to validate textfields according to regular expressions. Methods include validation
 * for First Name, Last Name, Phone Number, E-mail, Number, Password, Username, Number.
 */

public class RegexValidator {
	public String validateFirstName(String firstName) {
		if (firstName.compareTo("") == 0) {
			return "The First Name field is required.\n";
		} else if (!firstName.matches("[A-Z][a-zA-Z]*")) {
			return "The first name you entered is not of the correct form, it must only contain Upper/lower-case letters.\n";
		}
		return "";
	}

	public String validateLastName(String lastName) {
		if (lastName.compareTo("") == 0) {
			return "The Last Name field is required.\n";
		} else if (!lastName.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {
			return "The last name you entered is not of the correct form, it must only contain Upper/lower-case letters.\n";
		}
		return "";
	}

	public String validatePhoneNumber(String phone) {
		if (phone.compareTo("") == 0) {
			return "The Phone Number field is required\n";
		} else if (!phone.matches("\\d{5}\\d{2}\\d{6}")) {
			return "The phone number you entered is not of the correct form, it must be in the form of 00000-00-000000\n";
		}
		return "";
	}

	public boolean validateUsername(String username) {
		if (username.compareTo("") == 0 || !username.matches("^[a-z0-9_.-]{1,15}$")) {
			return false;
		}
		return true;
	}

	public String validateNumber(String number) {
		if (number.compareTo("") == 0) {
			return "The Product Price field is required.\n";
		} else if (!number.matches("([0-9]*)")) {
			return "The Product Price field must contain numbers only.\n";
		}
		return "";
	}

	public String validatePassword(String password, String confirmation) {
		if (password.compareTo("") == 0) {
			return "The Password field is required.\n";
		} else if (password.compareTo(confirmation) != 0) {
			return "The password you entered does not match its confirmation.\n";
		}
		return "";
	}

	public boolean validateEmail(String email) {
		String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		if (email.compareTo("") == 0 || !email.matches(emailPattern)) {
			return false;
		}
		return true;
	}

	public boolean validateDomain(String domain) {
		String ValidIP = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		String parts[] = domain.split("\\.");
		if (domain.compareTo("") == 0 || !domain.matches(ValidIP)) {
			return true;
		}
		return true;
	}
}
