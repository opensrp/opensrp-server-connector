package org.opensrp.connector.openmrs.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.connector.openmrs.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-applicationContext-opensrp-connector.xml")
public class HouseHoldServiceTest extends OpenmrsApiService {

    int firstIndex = 0;
    int secondIndex = 1;
    @Autowired
    private HouseholdService hhs;

    public HouseHoldServiceTest() throws IOException {
        super();
    }

    @Before
    public void setup() throws IOException {

    }

    @Test
    public void testCreateRelationshipTypeAndGetRelationshipType() throws JSONException {
        String AIsToB = "Mother";
        String BIsToA = "GrandMother";

        JSONObject returnRelationshipType = hhs.createRelationshipType(AIsToB, BIsToA, "test relationship");
        String expectedAIsToB = AIsToB;
        String expectedBIsToA = BIsToA;
        String actualAIsToB = returnRelationshipType.getString(aIsToBKey);
        String actualBIsToA = returnRelationshipType.getString(bIsToAKey);
        String uuid = returnRelationshipType.getString(uuidKey);
        assertEquals(expectedAIsToB, actualAIsToB);
        assertEquals(expectedBIsToA, actualBIsToA);
        String notSameRelation = "notSameRelation";
        assertNotSame(notSameRelation, actualBIsToA);
        JSONObject findRelationshipType = hhs.findRelationshipTypeMatching(AIsToB);

        String actualForFindRelationshipTypeBIsToA = findRelationshipType.getString(bIsToAKey);
        String actualForFindRelationshipTypeAIsToB = findRelationshipType.getString(aIsToBKey);
        assertEquals(expectedAIsToB, actualForFindRelationshipTypeAIsToB);
        assertEquals(expectedBIsToA, actualForFindRelationshipTypeBIsToA);
        JSONObject getRelationshipType = hhs.getRelationshipType(AIsToB);
        String actualForGetRelationshipTypeBIsToA = getRelationshipType.getString(bIsToAKey);
        String actualForGetRelationshipTypeAIsToB = getRelationshipType.getString(aIsToBKey);
        assertEquals(expectedAIsToB, actualForGetRelationshipTypeAIsToB);
        assertEquals(expectedBIsToA, actualForGetRelationshipTypeBIsToA);

        assertNotSame(notSameRelation, actualForGetRelationshipTypeBIsToA);
        deleteRelationshipType(uuid);
        JSONObject getRelationshipTypeWhichNotExists = hhs.getRelationshipType(notSameRelation);
        if (getRelationshipTypeWhichNotExists.has("error")) {
            System.out.println("Not Found");
        }

    }

    @Test
    public void testConvertRelationshipToOpenmrsJson() throws JSONException {
        String expectedPersonA = "personA";
        String expectedPersonB = "personB";

        JSONObject convertRelationshipToOpenmrsJson = hhs.convertRelationshipToOpenmrsJson(expectedPersonA,
                "isARelationship", expectedPersonB);
        String actualPersonA = convertRelationshipToOpenmrsJson.getString("personA");
        String actualPersonB = convertRelationshipToOpenmrsJson.getString("personB");
        assertEquals(expectedPersonA, actualPersonA);
        assertEquals(expectedPersonB, actualPersonB);
        String notSameRelation = "notSameRelation";
        assertNotSame(notSameRelation, actualPersonA);
        assertNotSame(notSameRelation, expectedPersonB);

    }

    @Test
    public void testConvertRelationshipTypeToOpenmrsJson() throws JSONException {
        String AIsToB = "Mother";
        String BIsToA = "GrandMother";
        JSONObject convertRelationshipTypeToOpenmrsJson = hhs.convertRelationshipTypeToOpenmrsJson(AIsToB, BIsToA,
                description);
        String expectedAIsToB = AIsToB;
        String expectedBIsToA = BIsToA;

        String actualAIsToB = convertRelationshipTypeToOpenmrsJson.getString(aIsToBKey);
        String actualBIsToA = convertRelationshipTypeToOpenmrsJson.getString(bIsToAKey);
        assertEquals(expectedAIsToB, actualAIsToB);
        assertEquals(expectedBIsToA, actualBIsToA);
        String notSameRelation = "notSameRelation";
        assertNotSame(notSameRelation, actualBIsToA);
    }


}
