package jp.gr.java_conf.nuranimation.my_bookshelf;


/**
 * Created by Kamada on 2019/03/11.
 */


public enum FragmentEvent {

    DISP_MASK {
        @Override
        public void apply(MainActivity activity){
            activity.dispMask(true);
        }
    },
    REMOVE_MASK {
        @Override
        public void apply(MainActivity activity){
            activity.dispMask(false);
        }
    },


    EVENT1 {
        @Override
        public void apply(MainActivity activity){
/*
            FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            mFragmentTransaction.replace(R.id.main_fragment_container,new Fragment_002());
//            mFragmentTransaction.addToBackStack("fragment1");
            mFragmentTransaction.commit();
            */
        }
    },
    EVENT2 {
        @Override
        public void apply(MainActivity activity) {
        }
    },
    EVENT3 {
        @Override
        public void apply(MainActivity activity) {

        }
    },
    EVENT4 {
        @Override
        public void apply(MainActivity activity) {
        }
    };

    abstract public void apply(MainActivity activity);

}
