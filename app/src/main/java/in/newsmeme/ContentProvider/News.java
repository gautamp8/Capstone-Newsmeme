package in.newsmeme.ContentProvider;

/**
 * Created by brainbreaker on 17/11/16.
 */

public class News {
    //private variables
    int _id;
    String _title;

    // Empty constructor
    public News(){

    }
    // constructor
    public News(int id, String title){
        this._id = id;
        this._title = title;
    }

    // constructor
    public News(String title){
        this._title = title;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting title
    public String getName(){
        return this._title;
    }

    // setting title
    public void setName(String title){
        this._title = title;
    }

}

