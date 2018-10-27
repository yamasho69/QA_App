package jp.techacademy.shohei.yamamoto.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private DatabaseReference mAnswerRef;
    public DatabaseReference mFavoriteRef;
    Boolean favorite_pressed = false;
    Boolean mFavorite = false;


    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            dataSnapshot.getValue();
            String uid = dataSnapshot.getKey();
            if(uid == null){
                mFavorite = false;
                ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite);
            }else {mFavorite = true;
                ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite_pressed);}
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {

                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        if(user != null){
        String uid = user.getUid();
        mFavoriteRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
        mFavoriteRef.addChildEventListener(mFavoriteListener);}
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

         ChildEventListener mFavoriteListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                dataSnapshot.getValue();
                String uid = dataSnapshot.getKey();
                if(uid == null){
                    ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite);
                }else {mFavorite = true;
                    ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite_pressed);}
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FloatingActionButton fav = (FloatingActionButton) findViewById(R.id.fav);
        if (user == null) {
            fav.setVisibility(View.INVISIBLE);
        } else {
            fav.setVisibility(View.VISIBLE);
            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            mFavoriteRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid());
            String uid = user.getUid();
            final String mQuestionUid = mQuestion.getUid ();
            final int mGenre = mQuestion.getGenre ();
            if (mFavorite == false) {
                ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite);
            }else { ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite_pressed);
                mFavorite = true;}
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mFavorite == false) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();
                        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference answerRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                        Map<String, String> data = new HashMap<String, String>();
                        // UID
                        data.put("genre", String.valueOf(mQuestion.getGenre ()));
                        answerRef.setValue(data);
                        Snackbar.make(view, "お気に入りに登録しました", Snackbar.LENGTH_LONG).show();
                        mFavorite = true;
                        ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite_pressed);
                    }else{
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();
                        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference answerRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                        Map<String, String> data = new HashMap<String, String>();
                        // UID
                        data.put("genre", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        answerRef.removeValue();
                        ((ImageView) findViewById(R.id.fav)).setImageResource(R.drawable.favorite);
                        mFavorite = false;
                        Snackbar.make(view, "お気に入りを解除しました", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}