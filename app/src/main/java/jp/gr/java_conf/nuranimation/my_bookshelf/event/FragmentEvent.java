package jp.gr.java_conf.nuranimation.my_bookshelf.event;


import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;

/**
 * Created by Kamada on 2019/03/11.
 */



public enum FragmentEvent {
    @SuppressWarnings("unused")
    EVENT1 {
        @Override
        public void apply(MainActivity activity) {
/*
            FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            mFragmentTransaction.replace(R.id.main_fragment_container,new Fragment_002());
//            mFragmentTransaction.addToBackStack("fragment1");
            mFragmentTransaction.commit();
            */
        }
    },
    @SuppressWarnings("unused")
    DEFAULT {
        @Override
        public void apply(MainActivity activity) {

        }
    };

    abstract public void apply(MainActivity activity);

}
