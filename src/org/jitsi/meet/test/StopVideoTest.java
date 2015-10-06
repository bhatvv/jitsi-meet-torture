/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

/**
 * To stop the video on owner and participant side.
 * @author Damian Minkov
 */
public class StopVideoTest
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public StopVideoTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new StopVideoTest("stopVideoOnOwnerAndCheck"));
        suite.addTest(new StopVideoTest("startVideoOnOwnerAndCheck"));
        suite.addTest(new StopVideoTest("stopVideoOnParticipantAndCheck"));
        suite.addTest(new StopVideoTest("startVideoOnParticipantAndCheck"));
        suite.addTest(new StopVideoTest(
            "stopOwnerVideoBeforeSecondParticipantJoins"));

        return suite;
    }

    /**
     * Stops the video on the conference owner.
     */
    public void stopVideoOnOwnerAndCheck()
    {
        System.err.println("Start stopVideoOnOwnerAndCheck.");

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(),
            "toolbar_button_camera");

        TestUtils.waitForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Starts the video on owner.
     */
    public void startVideoOnOwnerAndCheck()
    {
        System.err.println("Start startVideoOnOwnerAndCheck.");

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(),
            "toolbar_button_camera");

        // make sure we check at the remote videos on the second participant
        // side, otherwise if local is muted will fail
        TestUtils.waitForElementNotPresentByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[starts-with(@id, 'participant_')]"
                + "/span[@class='videoMuted']"
                + "/i[@class='icon-camera-disabled']", 10);
    }

    /**
     * Stops the video on participant.
     */
    public void stopVideoOnParticipantAndCheck()
    {
        System.err.println("Start stopVideoOnParticipantAndCheck.");

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_camera");

        TestUtils.waitForElementByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Starts the video on participant.
     */
    public void startVideoOnParticipantAndCheck()
    {
        System.err.println("Start startVideoOnParticipantAndCheck.");

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_camera");

        TestUtils.waitForElementNotPresentByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Stops video of the owner and then joins new participant and
     * checks the status of the stopped video icon.
     * At the end starts the video to clear the state.
     */
    public void stopOwnerVideoBeforeSecondParticipantJoins()
    {
        System.err.println("Start stopOwnerVideoBeforeSecondParticipantJoins.");

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(),
            "toolbar_button_camera");

        TestUtils.waitMillis(500);

        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();

        ConferenceFixture.waitForParticipantToJoinMUC(secondParticipant, 10);
        ConferenceFixture.waitForIceCompleted(secondParticipant);

        TestUtils.waitForElementByXPath(
            secondParticipant,
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']",
            5);

        // just debug messages
        {
            String ownerJid = (String) ((JavascriptExecutor)
                ConferenceFixture.getOwner())
                .executeScript("return APP.xmpp.myJid();");

            String streamByJid = "APP.RTC.remoteStreams['" + ownerJid + "']";
            System.err.println("Owner jid: " + ownerJid);

            Object streamExist = ((JavascriptExecutor)secondParticipant)
                .executeScript("return " + streamByJid + " != undefined;");
            System.err.println("Stream : " + streamExist);

            if (streamExist != null && streamExist.equals(Boolean.TRUE))
            {
                Object videoStreamExist
                    = ((JavascriptExecutor)secondParticipant).executeScript(
                        "return " + streamByJid + "['Video'] != undefined;");
                System.err.println("Stream exist : " + videoStreamExist);

                if (videoStreamExist != null && videoStreamExist
                    .equals(Boolean.TRUE))
                {
                    Object videoStreamMuted
                        = ((JavascriptExecutor) secondParticipant)
                            .executeScript(
                                "return " + streamByJid + "['Video'].muted;");
                    System.err.println("Stream muted : " + videoStreamMuted);
                }
            }
        }

        // now lets start video for owner
        startVideoOnOwnerAndCheck();

        // just in case wait
        TestUtils.waitMillis(1500);
    }

}
