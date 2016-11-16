This app contains a file called "./app/src/main/res/values/KEYS_and_SECRETS.xml" that is ignored by GitHub.

This file contains the various Keys, IDs, and Secrets for cloud and service interaction.
In order for this application to work you will need to create this file and populate accordingly.

The format of the file is as contained within the lines:

----------------------------------
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!--------------------------------------------------------------------------------------------->
    <!-- The Facebook application ID used to distinguish this app from others on Facebook -->
    <string name="facebook_application_id" translatable="false">xxxxxxxxxxxxx</string>

    <!-- Facebook Application ID, prefixed by 'fb'.  Enables Chrome Custom tabs. -->
    <string name="facebook_login_protocol_scheme" translatable="false">xxxxxxxxxxxx</string>

    <!-- The Facebook App Secret -->
    <string name="facebook_app_secret" translatable="false">xxxxxxxxxxxxxxxxxxxxxxx</string>
    <!--------------------------------------------------------------------------------------------->

    <!--------------------------------------------------------------------------------------------->
    <!-- The Twitter API Key -->
    <string name="twitter_consumer_key" translatable="false">xxxxxxxxxxxxxxxxxxxxx</string>

    <!-- The Twitter API Secret -->
    <string name="twitter_consumer_secret" translatable="false">xxxxxxxxxxxxxxxxxxxxxxx</string>
    <!--------------------------------------------------------------------------------------------->

</resources>
----------------------------------
