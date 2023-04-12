package tn.rhdev.mathquiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tn.rhdev.mathquiz.utils.Constant;

public class QuizHelper extends SQLiteOpenHelper {
    Context context;
    private static final int DATABASE_VERSION = 13;

    private static final String DATABASE_NAME = "mathsone";
    private static final String TABLE_QUEST = "quest";
    private static final String KEY_ID = "qid";
    private static final String KEY_QUES = "question";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_OPTA = "opta";
    private static final String KEY_OPTB = "optb";
    private static final String KEY_OPTC = "optc";
    private static final String KEY_TYPE = "type";

    private  SQLiteDatabase dbase;

    public QuizHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void deleteTable() {
        if (dbase == null || !dbase.isOpen())
            dbase = getWritableDatabase();
        dbase.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEST);
        onCreate(dbase);


    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        dbase = db;
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEST);
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_QUEST + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_QUES
                + " TEXT, " + KEY_ANSWER + " TEXT, " + KEY_OPTA + " TEXT, "
                + KEY_OPTB + " TEXT, " + KEY_OPTC + " TEXT, " + KEY_TYPE + " TEXT)";
        db.execSQL(sql);
        addQuestion();
        // db.close();
    }

    private void addQuestion() {
        addQuestionaddition();
        addQuestionsubtraction();
        addQuestionmultiplication();
        addQuestiondivision();

        // END
    }

    private void addQuestionaddition() {
        Random rand = new Random();
        for (int i = 0; i < Constant.questions_number; i++) {
            int one = rand.nextInt(Constant.max__number_value-1) + 1;
            int two = rand.nextInt(Constant.max__number_value-1) + 1;
            int anwsr=one+two;
            int optionone=rand.nextInt(anwsr-1) + 1;
            int optiontow=rand.nextInt(anwsr-1) + rand.nextInt(3);
            if (optionone==anwsr)
                optionone+=rand.nextInt(3);
            if (optiontow==anwsr)
                optiontow+=rand.nextInt(3);
            if (optionone==optiontow)
                optiontow+=rand.nextInt(3);

            List<Integer> solution = new ArrayList<>();
            solution.add(optionone);
            solution.add(optiontow);
            solution.add(anwsr);
            Collections.shuffle(solution);
            int caseone=solution.get(0);
            int casetwo=solution.get(1);
            int casethree=solution.get(2);

            Question q1 = new Question(one+" + "+two+" = ?", caseone+"", casetwo+"", casethree+"", "plus", anwsr+"");
            this.addQuestion(q1);
        }//for
    }        // END
    private void addQuestionsubtraction() {
        Random rand = new Random();
        for (int i = 0; i < Constant.questions_number; i++) {
            int one = rand.nextInt(Constant.max__number_value-1) + 1;
            int two = rand.nextInt(Constant.max__number_value-1) + 1;
            if (one==two)
                one+=1;
            if (two>one)
            {
                int aux =two;
                two=one;
                one=aux;

            }
            int anwsr=one-two;
            int optionone=rand.nextInt(anwsr) + 1;
            int optiontow=rand.nextInt(anwsr) + rand.nextInt(3);
            if (optionone==anwsr)
                optionone+=rand.nextInt(3);
            if (optiontow==anwsr)
                optiontow+=rand.nextInt(3);
            if (optionone==optiontow)
                optiontow+=rand.nextInt(3);

            List<Integer> solution = new ArrayList<>();
            solution.add(optionone);
            solution.add(optiontow);
            solution.add(anwsr);
            Collections.shuffle(solution);
            int caseone=solution.get(0);
            int casetwo=solution.get(1);
            int casethree=solution.get(2);

            Question q1 = new Question(one+" - "+two+" = ?", caseone+"", casetwo+"", casethree+"", "moins", anwsr+"");
            this.addQuestion(q1);
        }//for
    }        // END
    private void addQuestionmultiplication() {
        Random rand = new Random();
        for (int i = 0; i < Constant.questions_number; i++) {
            int one = rand.nextInt(Constant.max__number_value-1) + 1;
            int two = rand.nextInt(Constant.max__number_value-1) + 1;
            if (one==0) one+=1;
            if (two==0) two+=1;
            int anwsr=one*two;
            int optionone=rand.nextInt(anwsr) + 1;
            int optiontow=rand.nextInt(anwsr) + rand.nextInt(3);
            if (optionone==anwsr)
                optionone+=rand.nextInt(3);
            if (optiontow==anwsr)
                optiontow+=rand.nextInt(3);
            if (optionone==optiontow)
                optiontow+=rand.nextInt(3);

            List<Integer> solution = new ArrayList<>();
            solution.add(optionone);
            solution.add(optiontow);
            solution.add(anwsr);
            Collections.shuffle(solution);
            int caseone=solution.get(0);
            int casetwo=solution.get(1);
            int casethree=solution.get(2);

            Question q1 = new Question(one+" * "+two+" = ?", caseone+"", casetwo+"", casethree+"", "fois", anwsr+"");
            this.addQuestion(q1);
        }//for
    }        // END
    private void addQuestiondivision() {
        Random rand = new Random();

        int[] intArray = new int[20];
        intArray[0]=2;
        intArray[1]=3;
        intArray[2]=4;
        intArray[3]=5;



        for (int i = 0; i < Constant.questions_number; i++) {

            int divider = intArray[rand.nextInt(4)] ;
            List<Integer> divider_array = new ArrayList<>();
            for (int j=divider; j<=Constant.max__number_value; j++){
                if (j % divider == 0) {
                    divider_array.add(j);
                }//if
            }//for
            Collections.shuffle(divider_array);


            int one = divider_array.get(0);
            int two = divider;






            int anwsr=one/two;
            int optionone=rand.nextInt(anwsr) + 1;
            int optiontow=rand.nextInt(anwsr) + rand.nextInt(3);
            if (optionone==anwsr)
                optionone+=rand.nextInt(3);
            if (optiontow==anwsr)
                optiontow+=rand.nextInt(3);
            if (optionone==optiontow)
                optiontow+=rand.nextInt(3);

            List<Integer> solution = new ArrayList<>();
            solution.add(optionone);
            solution.add(optiontow);
            solution.add(anwsr);
            Collections.shuffle(solution);
            int caseone=solution.get(0);
            int casetwo=solution.get(1);
            int casethree=solution.get(2);

            Question q1 = new Question(one+" / "+two+" = ?", caseone+"", casetwo+"", casethree+"", "sur", anwsr+"");
            this.addQuestion(q1);
        }//for
    }        // END

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEST);

        onCreate(db);
    }


    public void addQuestion(Question quest) {


        ContentValues values = new ContentValues();
        values.put(KEY_QUES, quest.getQUESTION());
        values.put(KEY_ANSWER, quest.getANSWER());
        values.put(KEY_OPTA, quest.getOPTA());
        values.put(KEY_OPTB, quest.getOPTB());
        values.put(KEY_OPTC, quest.getOPTC());
        values.put(KEY_TYPE, quest.getTYPE());


        dbase.insert(TABLE_QUEST, null, values);
    }

    public List<Question> getAllQuestions(String method) {
        List<Question> quesList = new ArrayList<Question>();

        String selectQuery = "SELECT  * FROM " + TABLE_QUEST +" WHERE "+KEY_TYPE+" ='"+method+"' ";
        dbase = this.getReadableDatabase();
        Cursor cursor = dbase.rawQuery(selectQuery, null);

        while (cursor.moveToNext()) {
            Question quest = new Question();
            quest.setID(cursor.getInt(0));
            quest.setQUESTION(cursor.getString(1));
            quest.setANSWER(cursor.getString(2));
            quest.setOPTA(cursor.getString(3));
            quest.setOPTB(cursor.getString(4));
            quest.setOPTC(cursor.getString(5));
            quest.setTYPE(cursor.getString(6));

            quesList.add(quest);
        }

        return quesList;
    }


}
