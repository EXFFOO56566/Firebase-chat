package com.devlomi.fireapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.devlomi.fireapp.model.ExpandableContact;
import com.devlomi.fireapp.model.PhoneContact;
import com.devlomi.fireapp.model.realms.PhoneNumber;
import com.devlomi.fireapp.model.realms.User;
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.wafflecopter.multicontactpicker.ContactResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Telephone;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import io.realm.RealmList;

/**
 * Created by Devlomi on 03/08/2017.
 */

public class ContactUtils {


    //get contacts from phonebook
    public static List<PhoneContact> getRawContacts(Context context) {
        List<PhoneContact> contactsList = new ArrayList<>();

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        // Build adapter with contact entries
        Cursor mCursor = null;
        Cursor phoneNumCursor = null;
        try {
            mCursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

            while (mCursor.moveToNext()) {
                //get contact name
                String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                //get contact name
                String contactID = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                //create new phoneContact object
                PhoneContact contact = new PhoneContact();
                contact.setId(Integer.parseInt(contactID));
                contact.setName(name);


                //get all phone numbers in this contact if it has multiple numbers
                phoneNumCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{contactID}, null);

                phoneNumCursor.moveToFirst();


                //create empty list to fill it with phone numbers for this contact
                List<String> phoneNumberList = new ArrayList<>();


                while (!phoneNumCursor.isAfterLast()) {
                    //get phone number
                    String number = phoneNumCursor.getString(phoneNumCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


                    //prevent duplicates numbers
                    if (!phoneNumberList.contains(number))
                        phoneNumberList.add(number);

                    phoneNumCursor.moveToNext();
                }

                //fill contact object with phone numbers
                contact.setPhoneNumbers(phoneNumberList);
                //add final phoneContact object to contactList
                contactsList.add(contact);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null)
                mCursor.close();
            if (phoneNumCursor != null)
                phoneNumCursor.close();
        }

        return contactsList;

    }


    //format number to international number
    //if number is not with international code (+1 for example) we will add it
    //depending on user country ,so if the user number is +1 1234-111-11
    //we will add +1 in this case for all the numbers
    //and if it's contains "-" we will remove them
    private static String formatNumber(Context context, String countryCode, String number) {

        PhoneNumberUtil util = PhoneNumberUtil.createInstance(context);
        Phonenumber.PhoneNumber phoneNumber;
        String phone = number;
        try {
            //format number depending on user's country code
            phoneNumber = util.parse(number, countryCode);
            phone = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        //remove empty spaces and dashes and ()
        if (phone != null)
            phone = phone.replaceAll(" ", "")
                    .replaceAll("-", "")
                    .replaceAll("\\(","")
                    .replaceAll("\\)","");

        return phone;


    }

    //get the Contact name from phonebook by number
    public static String queryForNameByNumber(Context context, String phone) {
        String name = phone;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));

            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            return name;
        }
        return name;

    }


    //check if a contact is exists in phonebook
    public static boolean contactExists(Context context, String number) {
        if (number != null) {
            Cursor cur = null;
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            try {
                cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
                if (cur.moveToFirst()) {
                    return true;
                }

            } catch (Exception e) {
                return false;
            } finally {
                if (cur != null)
                    cur.close();
            }
            return false;
        } else {
            return false;
        }
    }


    public static void syncContacts(final Context context, final OnContactSyncFinished onContactSyncFinished) {
        //get user country code when he registered by his phone number
        String countryCode = SharedPreferencesManager.getCountryCode();

        //get contacts from device
        List<PhoneContact> rawContacts = getRawContacts(context);

        //if there are no contacts ,just notify the service that sync has finished
        if (rawContacts.isEmpty()) {
            if (onContactSyncFinished != null)
                onContactSyncFinished.onFinish();
        } else {

            //get last contact id to know when sync is finished
            final int lastItemId = rawContacts.get(rawContacts.size() - 1).getId();

            //iterate
            for (final PhoneContact contact : rawContacts) {

                final int id = contact.getId();

                //get every number in the contact
                for (String number : contact.getPhoneNumbers()) {

                    //format number to be like this +1222333666
                    final String phoneNumber = formatNumber(context, countryCode, number);


                    //check if number is not null and the number is not the user number (if he saved his number in contacts we don't want to show it)
                    if (phoneNumber != null && !phoneNumber.equals(FireManager.getPhoneNumber())) {
                        //check if contact has installed this app
                        FireManager.isHasFireApp(phoneNumber, new FireManager.IsHasAppListener() {
                            @Override
                            public void onFound(User user) {
                                //if user installed this app
                                //get user info
                                //get current  user from realm if exists
                                User storedUser = RealmHelper.getInstance().getUser(user.getUid());
                                //save name by get the contact from phone book
                                String name = queryForNameByNumber(context, phoneNumber);
                                boolean isStored = contactExists(context, phoneNumber);
                                //if user is not exists in realm save it
                                if (storedUser == null) {
                                    //save user name
                                    user.setUserName(name);
                                    user.setStoredInContacts(isStored);
                                    //save user with his info(photo,number,uid etc..)
                                    RealmHelper.getInstance().saveObjectToRealm(user);


                                } else {
                                    //if user is exists in database update his info if they are not same
                                    RealmHelper.getInstance().updateUserInfo(user, storedUser, name, isStored);
                                }

                                //if this is the last item notify service that the sync has finished
                                if (id == lastItemId) {

                                    if (onContactSyncFinished != null)
                                        onContactSyncFinished.onFinish();
                                }
                            }

                            //if user does not installed this app
                            @Override
                            public void onNotFound() {
                                //if this is the last item notify service that the sync has finished
                                if (id == lastItemId) {
                                    if (onContactSyncFinished != null)
                                        onContactSyncFinished.onFinish();
                                }

                            }
                        });

                    }
                }
            }

        }
    }


    public interface OnContactSyncFinished {
        void onFinish();
    }


    //convert vCard (contact) Text to organized vCard object
    //this is used when user shares a contact to our app
    //an it's shared as String vCard (vcf)
    public static List<VCard> getContactAsVcard(Context context, Uri uri) {

        ContentResolver cr = context.getContentResolver();
        InputStream stream = null;
        try {
            stream = cr.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuffer fileContent = new StringBuffer("");
        int ch;
        try {
            while ((ch = stream.read()) != -1)
                fileContent.append((char) ch);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = new String(fileContent);
        List<VCard> vCards = Ezvcard.parse(data).all();

        return vCards;
    }

    //convert vCard List to a List of ExpandableContact
    public static List<ExpandableContact> getContactNamesFromVcard(List<VCard> vcards) {

        List<ExpandableContact> contactNameList = new ArrayList<>();

        for (VCard vcard : vcards) {
            //get contact name
            String fullName = vcard.getFormattedName().getValue();
            //get contact numbers
            List<Telephone> telephoneNumbers = vcard.getTelephoneNumbers();
            //create new List to fill it with phone numbers
            RealmList<PhoneNumber> numberList = new RealmList<>();

            //add numbers to list
            for (Telephone telephoneNumber : telephoneNumbers) {
                numberList.add(new PhoneNumber(telephoneNumber.getText()));
            }

            //create new ExpandableContact object
            ExpandableContact contactName = new ExpandableContact(fullName, numberList);
            //add contact to final list
            contactNameList.add(contactName);
        }


        return contactNameList;
    }

    //convert the contacts that the user's picked into an ExpandableContact list
    public static List<ExpandableContact> getContactsFromContactResult(List<ContactResult> results) {

        List<ExpandableContact> contactList = new ArrayList<>();

        for (ContactResult result : results) {
            RealmList<PhoneNumber> phoneNumbers = new RealmList<>();

            for (com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber s : result.getPhoneNumbers()) {

                if (!phoneNumbers.contains(new PhoneNumber(s.getNumber())))
                    phoneNumbers.add(new PhoneNumber(s.getNumber()));
            }
            ExpandableContact contactName = new ExpandableContact(result.getDisplayName(), phoneNumbers);
            contactList.add(contactName);

        }
        return contactList;
    }


    //get only selected phone numbers
    public static List<ExpandableContact> getContactsFromExpandableGroups(List<? extends ExpandableGroup> groups) {
        List<ExpandableContact> contactNameList = new ArrayList<>();
        for (int x = 0; x < groups.size(); x++) {
            MultiCheckExpandableGroup group = (MultiCheckExpandableGroup) groups.get(x);

            String name = group.getTitle();
            RealmList<PhoneNumber> phoneNumberList = new RealmList<>();

            for (int i = 0; i < group.getItems().size(); i++) {
                PhoneNumber phoneNumber = (PhoneNumber) group.getItems().get(i);
                //get only selected numbers && prevent duplicate numbers
                if (group.selectedChildren[i] && !phoneNumberList.contains(phoneNumber)) {
                    phoneNumberList.add(phoneNumber);
                }
            }

            if (!phoneNumberList.isEmpty())
                contactNameList.add(new ExpandableContact(name, phoneNumberList));


        }
        return contactNameList;
    }


}
