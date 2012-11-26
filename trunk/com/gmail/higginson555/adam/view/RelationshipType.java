package com.gmail.higginson555.adam.view;

import java.util.ArrayList;

/**
 *
 * @author Adam
 */
public class RelationshipType 
{
    
    public static final RelationshipType CLOSE_FRIEND =     new RelationshipType("Close Friend");
    public static final RelationshipType FRIEND =           new RelationshipType("Friend");
    public static final RelationshipType ACQUAINTANCE =     new RelationshipType("Acquaintance");
    public static final RelationshipType FATHER =           new RelationshipType("Father");
    public static final RelationshipType MOTHER =           new RelationshipType("Mother");
    public static final RelationshipType BROTHER =          new RelationshipType("Brother");
    public static final RelationshipType SISTER =           new RelationshipType("Sister");
    public static final RelationshipType SON =              new RelationshipType("Son");
    public static final RelationshipType DAUGHTER =         new RelationshipType("Daughter");
    public static final RelationshipType COUSIN =           new RelationshipType("Cousin");
    public static final RelationshipType GRANDFATHER =      new RelationshipType("Grandfather");
    public static final RelationshipType GRANDMOTHER =      new RelationshipType("Grandmother");
    public static final RelationshipType UNCLE =            new RelationshipType("Uncle");
    public static final RelationshipType AUNT =             new RelationshipType("Aunt");
    public static final RelationshipType NEICE =            new RelationshipType("Neice");
    public static final RelationshipType NEPHEW =           new RelationshipType("Nephew");
    public static final RelationshipType GRANDSON =         new RelationshipType("Grandson");
    public static final RelationshipType GRANDDAUGHTER =    new RelationshipType("Granddaughter");
    public static final RelationshipType UNKNOWN =          new RelationshipType("Unknown");
    
    //A list containing all default relations
    private static final ArrayList<RelationshipType> ALL_DEFAULT_RELATIONS 
            = new ArrayList<RelationshipType>(20);
    
    static
    {
        ALL_DEFAULT_RELATIONS.add(CLOSE_FRIEND);
        ALL_DEFAULT_RELATIONS.add(FRIEND);
        ALL_DEFAULT_RELATIONS.add(FATHER);
        ALL_DEFAULT_RELATIONS.add(MOTHER);
        ALL_DEFAULT_RELATIONS.add(BROTHER);
        ALL_DEFAULT_RELATIONS.add(SISTER);
        ALL_DEFAULT_RELATIONS.add(SON);
        ALL_DEFAULT_RELATIONS.add(DAUGHTER);
        ALL_DEFAULT_RELATIONS.add(COUSIN);
        ALL_DEFAULT_RELATIONS.add(GRANDFATHER);
        ALL_DEFAULT_RELATIONS.add(GRANDMOTHER);
        ALL_DEFAULT_RELATIONS.add(UNCLE);
        ALL_DEFAULT_RELATIONS.add(AUNT);
        ALL_DEFAULT_RELATIONS.add(NEICE);
        ALL_DEFAULT_RELATIONS.add(NEPHEW);
        ALL_DEFAULT_RELATIONS.add(GRANDSON);
        ALL_DEFAULT_RELATIONS.add(GRANDDAUGHTER);
        ALL_DEFAULT_RELATIONS.add(UNKNOWN);
    }
    
    private String name;
    
    public RelationshipType(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
    
    public static ArrayList<RelationshipType> getDefaultRelationships()
    {
        return ALL_DEFAULT_RELATIONS;
    }
    
    
}
