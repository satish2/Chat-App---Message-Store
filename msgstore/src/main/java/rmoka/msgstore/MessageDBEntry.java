package rmoka.msgstore;

import android.os.Message;
import android.provider.BaseColumns;

/**
 * Created by Ramakant on 6/2/2015.
 * This class represents the constants involved in schema of database table, its columns.
 */
public class MessageDBEntry implements BaseColumns {
    public static final String TABLE_NAME = "messages";
    public static final String MSG_POSITION = "msgposition";
    public static final String MSG_CONTENT = "msgcontent";


}
