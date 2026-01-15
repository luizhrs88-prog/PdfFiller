package com.example.pdffiller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.navigation.fragment.NavHostFragment;
import com.example.pdffiller.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment  {
        private static final int OPEN_FOLDER_TO_READ = 323;
        private static final int OPEN_FILE_TO_READ = 223;

    private FragmentFirstBinding binding;
    MyFragmentListener callback;


    private static final int CREATEPDF_TO_WRITE = 613;
    private static final int OPEN_XLSDATA_TO_READ = 812;
    private static final int OPEN_PDFMODEL_TO_READ =365;
    private static final int OPEN_XLS_MODIFCOLUMNS = 524;
    private static final int FOLDERPDF_TO_WRITE =421;
    private static final int CREATE_HEADER_FILE_XLS= 316;


   private String pdfmodelpath;
   private String xlsdatapath;
   private String pdffilledpath;
   private String pdfintermpath;

   private int spinnermode;


    // private Button mFolderWatchButton;
        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState
        ) {

           // View root  =inflater.inflate(R.layout.fragment_first, container, false);

         //   mFolderWatchButton = (Button)  root.findViewById(R.id.button4);

          binding = FragmentFirstBinding.inflate(inflater, container, false);
          return binding.getRoot();

        }

        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
            });

          //  binding.mFolder

            binding.button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("DEBUG", "clicou code "+OPEN_FOLDER_TO_READ );
                    Intent intent = new Intent()
                            .setType("application/pdf")
                            .setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setFlags(intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

//                    registerForActivityResult()
                    getActivity().startActivityForResult(Intent.createChooser(intent, "Select pdf model"), OPEN_PDFMODEL_TO_READ);

                }
            });

            binding.button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("DEBUG", "clicou code "+CREATE_HEADER_FILE_XLS );





                    Intent intent = new Intent()
                            .setType("application/vnd.ms-excel")
                            //.setAction(Intent.ACTION_GET_CONTENT);
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra(Intent.EXTRA_TITLE, "modelfilefields.xls")
                            .setAction(Intent.ACTION_CREATE_DOCUMENT);

                    getActivity().startActivityForResult(intent, CREATE_HEADER_FILE_XLS);
//                    getActivity().startActivityForResult(Intent.createChooser(intent, "Select sheet file"), OPEN_XLS_MODIFCOLUMNS);

                    //      startActivityForResult(Intent.createChooser(intent, "Select a folder"), OPEN_FOLDER_TO_READ);

                }
            });


            binding.button4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("DEBUG", "clicou code "+OPEN_FOLDER_TO_READ );

                    Intent intent = new Intent()
                            .setType("application/vnd.ms-excel")
                            //.setAction(Intent.ACTION_GET_CONTENT);
                                    .setAction(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE);
                //    intent.addFlags(intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    intent.addFlags(intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.addFlags(intent.FLAG_GRANT_READ_URI_PERMISSION);



                    getActivity().startActivityForResult(Intent.createChooser(intent, "Select xls sheet file"), OPEN_XLSDATA_TO_READ);

              //      startActivityForResult(Intent.createChooser(intent, "Select a folder"), OPEN_FOLDER_TO_READ);

                }
            });

            binding.button5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("DEBUG", "clicou code "+FOLDERPDF_TO_WRITE );

/*
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_TITLE, "preenchido.pdf");
*/


                   // Intent.ACTION_OPEN_DOCUMENT | Intent.ACTION_P
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    //intent.addCategory(Intent.CATEGORY_OPENABLE);
                  //  intent.setType("resource/folder");
                  //  intent.putExtra(Intent.EXTRA_TITLE, "preenchido.pdf");


                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker when your app creates the document.
                  //  intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory().toURI());

                    getActivity().startActivityForResult(intent, FOLDERPDF_TO_WRITE);



                    //      startActivityForResult(Intent.createChooser(intent, "Select a folder"), OPEN_FOLDER_TO_READ);

                }
            });


            binding.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("DEBUG", "clicou code "+OPEN_FOLDER_TO_READ );


                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_TITLE, "preenchido.pdf");

                    getActivity().startActivityForResult(intent,    CREATEPDF_TO_WRITE);



                    //      startActivityForResult(Intent.createChooser(intent, "Select a folder"), OPEN_FOLDER_TO_READ);

                }
            });

            binding.checkBox2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                //    (MainActivity) getActivity()
                    sendDataToActivity(binding.checkBox2.isChecked());
                }
            });


// Create an ArrayAdapter using the string array and a default spinner layout.
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    getContext(),
                    R.array.open_file_choices,
                    android.R.layout.simple_spinner_item
            );
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner.
            binding.spinner2.setAdapter(adapter);//; getRoot().findViewById(R.id.spinner2);

            binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    Log.i("DEBUG"," fragment fdsfsdf "+ position + " fdsuhfuhd "+id );
                 //   Log.i("DEBUG"," fragment fdsfsdf "+ position + " fdsuhfuhd "+id );




                    callback.onsetOpenmode(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView){
                    Log.i("DEBUG"," nothing selected fdsfsdf ");
                }
            });




//        spinner.setAdapter(adapter);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }

    @Override
    public void onResume() {
            Log.i("DEBUG", " getid "+getId());
        super.onResume();
        Log.i("DEBUG", " pdfmodelf: "+ pdfmodelpath);

        if (pdfmodelpath != null && !pdfmodelpath.isEmpty())
            binding.textView3.setText(pdfmodelpath);
        if (xlsdatapath != null && !xlsdatapath.isEmpty())
            binding.textView4.setText(xlsdatapath);
        if (pdffilledpath != null && !pdffilledpath.isEmpty())
            binding.textView.setText(pdffilledpath);

        if (pdfintermpath != null && !pdfintermpath.isEmpty())
            binding.textView5.setText(pdfintermpath);

        binding.spinner2.setSelection(spinnermode);


    }

        public void setspinneropenmode(int mode) {
            Log.i("DEBUG", "chegou no set spinner fragment");
            spinnermode = mode;}


        public void setviewtextpdfmodel(String path){
            Log.i("DEBUG", "model file set path "+ path);

            pdfmodelpath = path;
        }

        public void setviewtextxlsdata(String path){
            xlsdatapath = path;
        }

        public void setviewtextpdffilled(String path){pdffilledpath = path;}

        public void setviewtextpdfinterm(String path) {pdfintermpath = path;}

        public void setprogressbar(int progress){
            binding.progressBar.setProgress(progress);

        }



        public void setinvisibleprogressbar(){
            binding.layoutprogress.setVisibility(View.INVISIBLE);
            //binding.progressBar.setVisibility(View.INVISIBLE);
        }
        public void setvisibleprogressbar(){
            binding.layoutprogress.setVisibility(View.VISIBLE);
            //binding.progressBar.setVisibility(View.VISIBLE);
        }

        public void initprogressbar(int minval, int maxval){
            binding.progressBar.setMin(minval);
            binding.progressBar.setMax(maxval);
        }





    public interface MyFragmentListener {
        void onDataSent(boolean data);

        void onsetOpenmode(int mode);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (MyFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MyFragmentListener");
        }
    }

    // ... later, when you want to send data
    private void sendDataToActivity(boolean flatten_param) {
        if (callback != null) {
            callback.onDataSent(flatten_param);
        }
    }


}