package com.example.administrator.sampleviewpager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.administrator.sampleviewpager.listviewSrcForWorkOut.WorkoutAdapter;
import com.example.administrator.sampleviewpager.listviewSrcForWorkOut.WorkoutItem;
import com.example.administrator.sampleviewpager.subSources.BasicInfo;

import static com.example.administrator.sampleviewpager.subSources.BasicInfo.REQ_MODIFY_WORKOUT;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_boolTimeSet;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_hour;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_mID;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_min;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_sec;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_timerSetting;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_workoutName;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_workoutNum;
import static com.example.administrator.sampleviewpager.subSources.KeySet.key_workoutSet;

public class FragmentWorkout extends Fragment
{
    public FragmentWorkout()
    {
    }


    ListView listViewForWorkout;

    Toolbar myToolbar;

    String woMenuState = BasicInfo.MENU_WO_NORMAL;

    boolean isMultMode = false;

    WorkoutAdapter workoutAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_workout, container, false);
        workoutAdapter = new WorkoutAdapter();

        listViewForWorkout = (ListView)layout.findViewById(R.id.listViewForWorkout);
        listViewForWorkout.setAdapter(workoutAdapter);

        workoutAdapter.addItem(new WorkoutItem(0, "벤치 프레스", "50", "3", "타이머 사용"));
        workoutAdapter.addItem(new WorkoutItem(1, "팔굽혀 펴기", "20", "5", "스톱워치 사용"));
        workoutAdapter.addItem(new WorkoutItem(2, "스쿼트", "100", "2", "사용 안함"));

        workoutAdapter.notifyDataSetChanged();


// 시작 상태, 삭제한 상태, 다중->단일로 갈때는 체크박스를 gone으로. 아니면 보이게!
        workoutAdapter.setCheckBoxState(false);

        setItemClicker(listViewForWorkout, workoutAdapter);
        setItemLongClicker(listViewForWorkout);

        //setSingleChoice(listViewForWorkout);


/////////////////////////////// 메모아이템을 수정한다.

///////////////롱클릭을 통한 수정 / 삭제 메뉴를 추가해야 한다.
        //////*롱클릭시 다중메뉴 활성화시키고 아이템을 선택시키자.





        return layout;
    }


    // 아이템 클릭 리스너를 활성화해준다.

    public void setItemClicker(final ListView lv, final Adapter adapter) {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //todo 어댑터가 woadapter아닌 그냥 adapter써도 되는지 확인할 것. 추후 버그 가능성

                WorkoutItem item = (WorkoutItem) adapter.getItem(position);

                // 수정 -- 메모 보기 액티비티 띄우기 -> 액티비티 따라 달라짐
                Intent intent = new Intent(getActivity().getApplicationContext(), AddWorkoutActivity.class);

                // intent.putExtra(BasicInfo.KEY_MEMO_MODE, BasicInfo.MODE_VIEW);
                intent.putExtra(BasicInfo.KEY_ADDWO_MODE, BasicInfo.MODE_MODIFY);


                intent.putExtra(key_mID, item.getmID());

                intent.putExtra(key_workoutName, item.getWoName().toString());
                intent.putExtra(key_timerSetting, item.getTimerSetting().toString());

                //시간을 세팅하지 않았다면 횟수와 세트만 전달하자.
                if (item.getBoolTimeSet() == false) {

                    intent.putExtra(key_boolTimeSet, item.getBoolTimeSet());
                    intent.putExtra(key_workoutNum, item.getWoNum());
                    intent.putExtra(key_workoutSet, item.getWoSet());
                }
                //시간을 세팅했다면 시간 + 세트를 전달해서 뿌려라.
                else {
                    Log.d("ggwp", "here im : booltimeset = " + item.getBoolTimeSet());

                    intent.putExtra(key_boolTimeSet, item.getBoolTimeSet());

                    intent.putExtra(key_workoutSet, item.getWoSet());

                    intent.putExtra(key_hour, item.getHour());
                    intent.putExtra(key_min, item.getMin());
                    intent.putExtra(key_sec, item.getSec());

                }


                // 모든 선택 상태 초기화.
                lv.clearChoices();
                workoutAdapter.notifyDataSetChanged();

                startActivityForResult(intent, REQ_MODIFY_WORKOUT);
                //////////////////


            }
        });
    }

    public void setItemLongClicker(final ListView lv) {

        //멀티모드가 아니었다. 멀티모드로 만든다.

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (isMultMode == false) {
                    lv.clearChoices();

                    setMultipleChoice(lv);


                    lv.setItemChecked(position, true);

                    isMultMode = true;


                    return true;
                }
                //멀티모드면 미리 선택된 아이템부터~ 선택한 아이템까지 모두 선택처리.
                else {

                    int lastPosition = -1;

                    for (int i = 0; i < workoutAdapter.getCount(); i++) {

                        if (listViewForWorkout.isItemChecked(i) == true) {
                            lastPosition = i;
                        }

                    }
                    //마지막에 선택한 아이템이 없을 경우
                    if (lastPosition == -1) {
                        lv.setItemChecked(position, true);
                        return true;
                    }

                    // 마지막에 선택한 아이템 존재! 얘부터 포지션까지 체크처리한다.
                    else {
                        // 마지막 선택 포지션이 지금 포지션과 비교해서 큰?작? 같?에 따라 다른가??

                        if (lastPosition < position) {
                            for (int i = lastPosition; i <= position; i++) {
                                lv.setItemChecked(i, true);
                            }

                        } else if (lastPosition > position) {
                            for (int i = lastPosition; i >= position; i--) {
                                lv.setItemChecked(i, true);
                            }

                        } else {
                            lv.setItemChecked(position, false);
                            return true;
                        }
                    }
                }


                return true;
            }
        });

        //멀티모드이므로 활성화되지 않음


    }


    /////////////////////////////단일 선택 / 다중 선택을 선택하는 모드.!!


    public void setSingleChoice(ListView lv) {

        //  Toast.makeText(getApplicationContext(), "단일 선택 모드로 변경되었습니다.", Toast.LENGTH_SHORT).show();

        lv.clearChoices();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        workoutAdapter.setCheckBoxState(false);

        setItemClicker(listViewForWorkout, workoutAdapter);

        ////////////// 메뉴를 원상복귀시킴
        woMenuState = BasicInfo.MENU_WO_NORMAL;
        isMultMode = false;
       // invalidateOptionsMenu();
    }

    public void setMultipleChoice(ListView lv) {
        // Toast.makeText(getApplicationContext(), "다중 선택 모드로 변경되었습니다.", Toast.LENGTH_SHORT).show();


        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        workoutAdapter.setCheckBoxState(true);


        //아이템클릭리스너를 무효화한다.
        lv.setOnItemClickListener(null);

        woMenuState = BasicInfo.MENU_WO_MULT;
        isMultMode = true;
       // invalidateOptionsMenu();
    }

}

