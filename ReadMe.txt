This App has a text input area where user can type messages. Then a save button to the side, to save the message in the ListView below this EditText view.
Clicking on a message in list view copies the message to clipboard. Long press on the message pops-up a context menu asking if user wants to Edit/Delete/Share
the message. Edit will take to another screen(or Activity) which has similar EditText view and Save button. Delete will delete the message and Share will allow
user to share message with other apps that are compatible to take this message.

Also, message are stored in SQLite database so that messages in ListView are not lost on closing the app.