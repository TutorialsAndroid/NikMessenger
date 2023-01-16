package com.messenger.nik.helper;

public class Constants {
    public static String CONTACTS_DB_PATH = null;

    //CONTACT ADDED DATABASE CHILD NAME
    public static final String CONTACTS_DB = "added_contacts";

    //REGISTERED VIRTUAL NUMBER DATABASE CHILD NAME
    public static final String DB_VN = "registered_virtual_numbers";

    //RECENT CHAT DATABASE CHILD NAME
    public static final String DB_MAIN = "recent_chats";

    //GROUP INFO STORE DATABASE CHILD NAME
    public static final String DB_GRP = "groups_info";

    //CHAT ROOM DATABASE CHILD NAME
    public static final String DB_CR = "chatRoom";

    //USER STATUS DATABASE CHILD NAME
    public static final String DB_US = "userStatus";

    //FIREBASE STORAGE PATHS
    public static final String URL_STORAGE_REFERENCE = "gs://nik-messenger-bb3a4.appspot.com";
    public static final String FOLDER_STORAGE_IMG = "/files";
    public static final String QUICK_STATUS = "/quickStatus";

    //REQUESTING FILE CHILD NAME
    public static final String REQ_FILE = "requestingFile";
    //GROUP REQUESTING FILE CHILD NAME
    public static final String GRP_REQ_FILE = "groupReqFile";

    //DENY FILE CHILD NAME
    public static final String DENY_FILE = "denyFile";

    //ALLOWING FILE CHILD NAME
    public static final String ALL_FILE = "allowingFile";
    //ALLOWING FILE GROUP CHILD NAME
    public static final String GRP_ALL_FILE = "GrpAllowFile";

    public static final String GRP_INFO = "groupsInfo";

    public static String current_user_virtual_number = null;
    public static String current_user_name = null;
    public static String current_user_avatar = null;

    //DEFAULT SHARED-PREF FILE NAME
    public static final String SP = "nikApp";
}
