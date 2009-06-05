/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
package org.jboss.seam.example.dvd.test.selenium;

/**
 * This class is used by Registration tests
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class Person {

    private String username;
    private String password;
    private String verify;
    private String firstName;
    private String lastName;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String email;
    private String phone;
    private String cardType;
    private String cardNumber;

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Person() {
    }

    public Person(String address, String address2, String cardNumber,
            String cardType, String city, String email, String firstName,
            String lastName, String password, String phone, String state,
            String username, String verify, String zip) {
        this.address = address;
        this.address2 = address2;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.city = city;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.phone = phone;
        this.state = state;
        this.username = username;
        this.verify = verify;
        this.zip = zip;
    }

    public Person(String address, String cardNumber, String cardType,
            String city, String email, String firstName, String lastName,
            String password, String phone, String state, String username,
            String verify, String zip) {
        this(address, address, cardNumber, cardType, city, email, firstName,
                lastName, password, phone, state, username, verify, zip);
    }

    public Person(String address, String address2, String city, String email,
            String firstName, String lastName, String password, String phone,
            String state, String username, String verify, String zip) {
        this(address, address2, "MasterCard", "000-0000-0000", city, email,
                firstName, lastName, password, phone, state, username, verify,
                zip);
    }

    public Person(String address, String city, String email, String firstName,
            String lastName, String password, String phone, String state,
            String username, String verify, String zip) {
        this(address, address, "MasterCard", "000-0000-0000", city, email,
                firstName, lastName, password, phone, state, username, verify,
                zip);
    }

    public Person(String username, String password, String verify) {
        super();
        this.password = password;
        this.username = username;
        this.verify = verify;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
