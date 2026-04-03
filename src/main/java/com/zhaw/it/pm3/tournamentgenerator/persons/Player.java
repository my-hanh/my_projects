package com.zhaw.it.pm3.tournamentgenerator.persons;

/**
 * Represents a player in the tournament system.
 * A player is a specific type of {@link Person} with additional attributes such as points scored, age, and email.
 */
public class Player extends Person {

    private int pointsScored;

    private int age;

    private String email;

    /**
     * Constructs a {@code Player} object with the specified name.
     * The player's points are initialized to 0, and the user rights are set to {@link UserRights#READ}.
     *
     * @param name the name of the player.
     */
    public Player(String name) {
        super(name);
        this.pointsScored = 0;
        userRights = UserRights.READ;
    }

    /**
     * Constructs a {@code Player} object with the specified name and email.
     * The player's points are initialized to 0, and the user rights are set to {@link UserRights#READ}.
     *
     * @param name  the name of the player.
     * @param email the email address of the player.
     */
    public Player(String name, String email) {
        super(name);
        this.email = email;
        this.pointsScored = 0;
        userRights = UserRights.READ;
    }

    /**
     * Sets the email address of the player.
     *
     * @param email the email address to set.
     */
    public void setEmail(String email) {
    }

    /**
     * Gets the email address of the player.
     *
     * @return the email address of the player.
     */
    public String getEmail() {
        return email;
    }

    /* For future implementation
    public int getPointsScored() {
        return pointsScored;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setPointsScored(int pointsScored) {
        this.pointsScored = pointsScored;
    }

    public void addPoint(int points) {
        this.pointsScored += points;
    }
    */
}