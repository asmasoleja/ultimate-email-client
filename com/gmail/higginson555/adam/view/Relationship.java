package com.gmail.higginson555.adam.view;

import java.util.ArrayList;

/**
 * A relationship is some way of identifying a list of e-mail addresses to
 * a particular e-mail address.
 * 
 * One example of this could be the 'Friend' relationship, which would have
 * a name of your friend, and then a list of e-mail addresses your friend uses.
 * 
 * @author Adam
 */
public class Relationship 
{        
    //The forenames of a person
    private String forenames;
    //The surname of a person
    private String surname;
    //The relationship type of this person
    private RelationshipType relationshipType;
    //The list of e-mail addresses used by this person
    private ArrayList<String> addresses;
    
    public Relationship(String forenames, String surname, 
            RelationshipType relationshipType, ArrayList<String> addresses)
    {
        this.forenames = forenames;
        this.surname = surname;
        this.relationshipType = relationshipType;
        this.addresses = addresses;
    }

    public String getForenames() {
        return forenames;
    }

    public String getSurname() {
        return surname;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public ArrayList<String> getAddresses() {
        return addresses;
    }
    
    public void addAddress(String address)
    {
        addresses.add(address);
    }
    
}
