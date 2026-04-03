package com.zhaw.it.pm3.tournamentgenerator.persons;

/**
 * Represents an administrator in the system.
 * Inherits from {@link Person} and has additional attributes like email credentials and a password.
 */
public class Administrator extends Person {

    private String SenderEmail;

    private String SenderPassword;

    private String password;

    /**
     * Constructs an {@code Administrator} object with the specified name.
     * Automatically assigns {@link UserRights#ADMIN} to the administrator.
     *
     * @param name the name of the administrator.
     */
    public Administrator(String name) {
        super(name);
        userRights = UserRights.ADMIN;
    }

    /**
     * Gets the sender email address associated with this administrator.
     *
     * @return the sender email address.
     */
    public String getSenderEmail() {
        return SenderEmail;
    }

    /**
     * Sets the sender email address for this administrator.
     *
     * @param SenderEmail the sender email address to set.
     */
    public void setSenderEmail(String SenderEmail) {
        this.SenderEmail = SenderEmail;
    }

    /**
     * Gets the sender email password associated with this administrator.
     *
     * @return the sender email password.
     */
    public String getSenderPassword() {
        return SenderPassword;
    }

    /**
     * Sets the sender email password for this administrator.
     *
     * @param SenderPassword the sender email password to set.
     */
    public void setSenderPassword(String SenderPassword) {
        this.SenderPassword = SenderPassword;
    }

    /**
     * Gets the password for this administrator.
     *
     * @return the password of the administrator.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this administrator.
     *
     * @param password the password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

}