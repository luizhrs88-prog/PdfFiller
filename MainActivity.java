package com.example.pdffiller;

import android.Manifest;
import android.app.Fragment;
import android.app.Notification;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.example.pdffiller.databinding.ActivityMainBinding;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.ByteArray;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


// permitir opcao de gerar arquivo finalizado ineditavel (flatten)
// quando o arquivo de dados tiver mais de uma linha , colocar em varios arquivos modelos pdf








public class MainActivity extends AppCompatActivity implements FirstFragment.MyFragmentListener, ProgressCallback, AdapterView.OnItemSelectedListener {
    private static final int OPEN_FOLDER_TO_READ = 323;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 956;
    private static final int CREATEPDF_TO_WRITE = 613;
    private static final int OPEN_XLSDATA_TO_READ = 812;
    private static final int OPEN_PDFMODEL_TO_READ = 365;
    private static final int OPEN_XLS_MODIFCOLUMNS = 524;
    private static final int FOLDERPDF_TO_WRITE = 421;
    private static final int CREATE_HEADER_FILE_XLS= 316;


    private boolean conffileexist = false;

    Uri uripath;

    private Uri xlsdatafile;
    private Uri pdfmodelfile;
    private Uri pdfmergedfile;
    private Uri pdfintermfolder;

    public boolean flatten = true;

    public boolean openfolder = false;
    public boolean openpdf = true;
    public boolean openapp = false;

    private Set<String> commonfieldnames; //= form.getFields().keySet();
    private Set<String> modelfieldnames;
    private Set<String> datafieldnames;

    // private String[] xlsdata_fiels

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    //WatchService watcher;// = FileSystems.getDefault().newWatchService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        datafieldnames = new HashSet<>();
        commonfieldnames = new HashSet<>();
        modelfieldnames = new HashSet<>();


        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //tentando pegar o fragmento para enviar os paths para colocar no textview
        // Fragment ff = getFragmentManager().findFragmentById(R.id.FirstFragment);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Snackbar.make(view, "Ativando monitor de diretorio", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });





        // Intent serviceIntent = new Intent(getBaseContext(),  FileMonitoringService.class);
        // startService(serviceIntent);

//        PdfReader reader = new PdfReader();
        // para androids mais novos   foreground service com  watchservice
//        Intent ni = new Intent(this, MyForegroundWatchService.class);
        //       startService(ni);
        // startService(new Intent(this, MyForegroundWatchService.class));
        initial_load_param();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("DEBUG", "chegou no final" + requestCode + " resultcode " + resultCode);

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
        FirstFragment childFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

        if (requestCode == OPEN_PDFMODEL_TO_READ && resultCode == RESULT_OK) {
            pdfmodelfile = data.getData();

            Log.i("DEBUG", " definindo arquivo de modelo " + pdfmodelfile);

            if (pdfmodelfile != null && !pdfmodelfile.getPath().isEmpty()) {
                getContentResolver().takePersistableUriPermission(pdfmodelfile, Intent.FLAG_GRANT_READ_URI_PERMISSION);





                File urimodfile = new File(getFilesDir(), "pdfmodelfile");

//                saveUritoFile(pdfmodelfile, urimodfile);
                saveUritoFile2(pdfmodelfile.toString(), urimodfile);

                saveparameter_conffile("pdfmodelfile", pdfmodelfile.getPath());

                // A FAZER
                // salvar pdfmodelfile no arquivo de configuracao pertinente
                // abrir o modelo e salvar campos em setdatafn();
                // atualizar interface com detalhes deste arquivo


                // opening and getting information from model file

                read_header_modelfile();
            }


        }

        if (requestCode == OPEN_XLSDATA_TO_READ && resultCode == RESULT_OK) {

            xlsdatafile = data.getData();
            Log.i("DEBUG", " definindo arquivo de dados " + xlsdatafile);
            //Log.i("DEBUG", " definindo arquivo de dados " + xlsdatafile.get);


            if (xlsdatafile != null && !xlsdatafile.getPath().isEmpty()) {
                // very long string to show in screen so  gonna take only lastsegment
                //childFragment.setviewtextxlsdata(xlsdatafile.getPath());
                // placed on onResume
                //childFragment.setviewtextxlsdata(xlsdatafile.getLastPathSegment());

                saveparameter_conffile("xlsdatafile", xlsdatafile.getPath());

                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


                getContentResolver().takePersistableUriPermission(xlsdatafile, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                File urimodfile = new File(getFilesDir(), "xlsdatafile");

//                saveUritoFile(pdfmodelfile, urimodfile);
                saveUritoFile2(xlsdatafile.toString(), urimodfile);

                List<UriPermission> lu = getContentResolver().getPersistedUriPermissions();
                for (int e = 0; e < lu.size(); e++) {
                    Log.i("DEBUG", e + "persist path" + lu.get(e).getUri().getPath());
                    Log.i("DEBUG", e + "persist read permission" + lu.get(e).isReadPermission());
                }

                read_header_datafile();

            }
        }

        if (requestCode == CREATEPDF_TO_WRITE && resultCode == RESULT_OK) {
            Log.i("DEBUG", "chegou no final" + requestCode + " resultcode " + resultCode);

            pdfmergedfile = data.getData();

            //permissao persistente para o diretorio
            getContentResolver().takePersistableUriPermission(
                    pdfmergedfile,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            Log.i("DEBUG", " definindo arquivo a ser criado com dados preenchidos " + pdfmergedfile);

            //FragmentManager fragmentManager = getSupportFragmentManager();
            //FirstFragment childFragment = (FirstFragment) fragmentManager.findFragmentById(R.id.FirstFragment);
            //childFragment.setviewtextpdffilled(pdfmergedfile.getPath());


           // merge2();

            String ofn = getFileNameFromUri(pdfmergedfile);
            childFragment.setviewtextpdffilled(ofn);

            //just initial value , real value will be set on merge3
            childFragment.initprogressbar(0,100);
            childFragment.setvisibleprogressbar();



            //ProgressCallback

            ExecutorService executor = Executors.newSingleThreadExecutor();
            MergeService ms = null;
            try {
                ms = new MergeService(1,this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Future<Integer> future = executor.submit(ms);


            executor.shutdown();

//            executor.submit(MergeService)

        }

        if (requestCode == CREATE_HEADER_FILE_XLS && resultCode == RESULT_OK) {
            Log.i("DEBUG", " create xls file with model file fields");
            /*
            pdfintermfolder = data.getData();

            // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//            int cid = navController.getCurrentDestination().getId();


            //         cid = R.id.nav_host_fragment_content_main;


            childFragment.setviewtextpdfinterm(pdfintermfolder.getPath());

            */
            Uri createheaderfile = data.getData();

            writeFile(createheaderfile);

        }


    }

    private int merge() {
        //abertura dos arquivos 2 de leitura  e 1 de saida

        List<String> temp_pdfs = new ArrayList<>();
        List<String> onlyfilenames = new ArrayList<>();

        try {


            String[] nome_campos;// =  new String[sdados.getRow(0).length];
            String[] valores_campos;// =  new String[sdados.getRow(0).length];
            int[] missing_fields;

            if (xlsdatafile == null || pdfmodelfile == null) return -1;

            InputStream inputdata = getContentResolver().openInputStream(xlsdatafile);
            Workbook workbook = Workbook.getWorkbook(inputdata);// createWorkbook(fileOutputStream);
            //WritableSheet sheet = workbook.createSheet("Dados", 0);
            String[] sheets = workbook.getSheetNames();
            Sheet sdados = workbook.getSheet(0);
            Log.i("DEBUG", "nome da planilha: " + sdados.getName());

            int datalines = sdados.getRows() - 1;


            for (int x = 1; x <= datalines; x++) {
                InputStream input = getContentResolver().openInputStream(pdfmodelfile);// .OpenInputStream(data.getData);

                PdfReader reader = new PdfReader(input);

                //pdfmergedfill
                String lastsegment = pdfmergedfile.getLastPathSegment();// Segments();
                lastsegment = "result.pdf";
                String folderpathsegment = pdfmergedfile.getPath().substring(0, pdfmergedfile.getPath().length() - lastsegment.length());
//                ps.get(ps.size()-1);
                folderpathsegment = pdfmergedfile.getPath() + "/";
                // pdfmergedfile.get
                // Uri cpdfmergedfile;
                // cpdfmergedfile.b

                String lsnoext = lastsegment.substring(0, lastsegment.length() - 4);
                String ext = lastsegment.substring(lastsegment.length() - 4);
                Uri.Builder b = new Uri.Builder();
                b.path(folderpathsegment + lsnoext + "-" + x + ext);
                Uri finalpdfmergedfile = b.build();

                Log.i("DEBUG", "result path: " + finalpdfmergedfile.getPath());
                // ContentProvider
                // getContentResolver().openOutputStream(finalpdfmergedfile);

                //      OutputStream outputFile = getContentResolver().openOutputStream(finalpdfmergedfile);
                File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), lsnoext + "-" + x + ext);
                //   File outputFile =  new File(pdfmergedfile.getPath()+"/", lsnoext+"-"+x+ext);

                //  outputFile.createNewFile();
                //DocumentFile pickedDir = DocumentFile.fromTreeUri(this, pdfmergedfile);
                //  DocumentFile newFile = pickedDir.createFile("application/pdf", lsnoext+"-"+x+ext);

                String outfilename = lsnoext + "-" + x + ext;
                String outfilename_noext = lsnoext + "-" + x;
                DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
                DocumentFile newFile = AppDir.createFile("application/pdf", outfilename_noext);


                temp_pdfs.add(newFile.getUri().getPath());
                onlyfilenames.add(outfilename_noext);
                OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                PdfStamper stamper = new PdfStamper(reader, os);

                Log.i("DEBUG", "arquivos de intermediario: " + newFile.getUri().getPath());

//                FileOutputStream fos = new FileOutputStream(outputFile);
//                PdfStamper stamper = new PdfStamper(reader, fos);


                // 2. Obter os campos do formulário
                AcroFields form = stamper.getAcroFields();
                //merge


                nome_campos = new String[sdados.getRow(0).length];
                valores_campos = new String[sdados.getRow(0).length];


                for (int ind = 0; ind < sdados.getRow(0).length; ind++) {
                    Cell c = sdados.getCell(ind, 0);
                    nome_campos[ind] = c.getContents();
                    c = sdados.getCell(ind, x);
                    valores_campos[ind] = c.getContents();
                }


                Set<String> fieldNames = form.getFields().keySet();

                for (int q = 0; q < nome_campos.length; q++) {
                    Log.i("DEBUG", "arquivo dados nome campos: " + nome_campos[q]);
                }

                Iterator<String> e = fieldNames.iterator();
                for (int q = 0; q < fieldNames.size(); q++) {
//                e.
                    Log.i("DEBUG", "arquivo modelo nome campos: " + e.next());
                }


                //  HashMap<String, String> setxls = new HashMap<String, String>();
                missing_fields = new int[nome_campos.length];
                int count_missingfields = 0;
                for (int r = 0; r < nome_campos.length; r++) {
                    missing_fields[r] = 0;
                    if (!fieldNames.contains(nome_campos[r])) {
                        missing_fields[r] = 1;
                        count_missingfields++;
                    }

                }

                //bug do itext versao  4.2 necessita chamra este setappearences  para o flattening funcionar
                //https://www.google.com/search?q=itext++4.2+setFormFlattening+turning+empty+document&client=firefox-b-d&sca_esv=d88ae73f6018b198&ei=_DEPaeqdGdPc1sQPnMmzmAY&ved=0ahUKEwjqi-fjweKQAxVTrpUCHZzkDGMQ4dUDCBA&uact=5&oq=itext++4.2+setFormFlattening+turning+empty+document&gs_lp=Egxnd3Mtd2l6LXNlcnAiM2l0ZXh0ICA0LjIgc2V0Rm9ybUZsYXR0ZW5pbmcgdHVybmluZyBlbXB0eSBkb2N1bWVudDIFEAAY7wUyBRAAGO8FMggQABiABBiiBDIIEAAYgAQYogQyBRAAGO8FSIIgUKUOWIkdcAJ4AJABAJgBzAGgAcUHqgEFMC40LjG4AQPIAQD4AQGYAgWgAsMFwgIIEAAYsAMY7wXCAgsQABiABBiwAxiiBMICChAhGKABGMMEGAqYAwCIBgGQBgWSBwUyLjAuM6AH8BayBwMyLTO4B4gFwgcFMy0zLjLIB00&sclient=gws-wiz-serp
                if (flatten) form.setGenerateAppearances(true);

                // 3. Preencher os campos
                if (count_missingfields == 0) {
                    for (int r = 0; r < nome_campos.length; r++) {
                        if (missing_fields[r] == 0)
                            form.setField(nome_campos[r], valores_campos[r]);

                    }
                }


                // Opcional: remover a interatividade dos campos (PDF achatado)


                if (flatten) stamper.setFormFlattening(true);


                Log.i("DEBUG", "  fechando  ");


                // 4. Fechar o stamper
                stamper.close();
                reader.close();

            }

            Log.i("DEBUG", "  vai salvar intermediarios?" + (pdfintermfolder != null));


            if (pdfintermfolder != null) {
                //copiar do pdfs intermediarios pro diretorio desginado pelo usuario

                //  OutputStream(outputPath);
                for (int r = 0; r < temp_pdfs.size(); r++) {


                    String sourcePdfPath = temp_pdfs.get(r);
                    String fn = onlyfilenames.get(r);
                    File sourcefile = new File(sourcePdfPath);


                    File destfile = new File(pdfintermfolder.getPath(), fn);//   eUri(this, pdfmergedfile);
                    //  DocumentFile newFile = pickedDir.createFile("application/pdf", lsnoext+"-"+x+ext);

                    // getContentResolver().openInputStream( sourcePdfPath);

                    Uri.Builder ofb = new Uri.Builder();
                    ofb.path(pdfmergedfile.getPath());
                    ofb.appendPath(sourcePdfPath);
                    Uri outf = ofb.build();


                    Document document = new Document();


                    Uri.Builder turi = pdfintermfolder.buildUpon().appendPath(fn);
                    //  Uri    turi.build();

                    //    OutputStream os = getContentResolver().openOutputStream(,);
                    File outputFile = new File(pdfintermfolder.getPath(), fn);


                    //   File outputFile  = new File(pdfintermfolder.getPath(), fn);
                    //    outputFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    PdfCopy copy = new PdfCopy(document, fos);
                    document.open();

//                    copy.open();

                    PdfReader reader2 = new PdfReader(sourcePdfPath);

                    copy.addDocument(reader2);
                    reader2.close();


                    document.close();

                    // File destDir = new File(pdfmergedfile.getPath());

                    //FileCopier.copyFileUsingStreams(sourcefile, destfile );
                }

            }


            mergePdfs(temp_pdfs, pdfmergedfile);


            //deletando arquivos intermediario na pasta do aplicativo
            for (int r = 0; r < temp_pdfs.size(); r++) {


                String sourcePdfPath = temp_pdfs.get(r);
                File fileToDelete = new File(sourcePdfPath); // For internal storage
                // Or for cache: File fileToDelete = new File(context.getCacheDir(), "my_cache_file.tmp");
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                    Log.i("DEBUG", " deletou arquivo: " + sourcePdfPath);

                }
            }
            //reader2.close();
            Log.i("DEBUG", " depois de close ");

            //fechamento e salvamento do arquivo pdf
        } catch (Exception e) {
            e.printStackTrace();
        }


        return 0;
    }


/*
    private void generatePdf() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample_01.pdf");
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);
            doc.add(new Paragraph("Hello from iText in Android!"));
            doc.close();
          //  Toast.makeText(this, "PDF saved:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
           // Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

*/

    // In your Activity
    // receber dados do fileobserver que detecta alteracoes de arquivos em uma pasta
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String path = intent.getStringExtra("path");

            Log.i("DEBUG", "abrindo arquivo xls: " + path);
            //     Snackbar.make(, "abrindo arquivo xls: "+path, Snackbar.LENGTH_LONG)


            //   getContentResolver().
            //abrir aqruivo

            File nf = new File(path);
            Log.i("DEBUG", "pode ler: " + nf.canRead());
            //Log.i("DEBUG","abrindo arquivo xls: "+path);

//            InputStream input = getContentResolver().openInputStream(nf.);


            //  Workbook workbook = Workbook.getWorkbook(input);// createWorkbook(fileOutputStream);
            //Workbook.getWorkbook()
            //readxlsFile( path, new String[0], new String[0], "");

            // Update UI or handle message
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("com.example.ACTION_SERVICE_MESSAGE"));


        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
        FirstFragment childFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();


        //carregar variaveis
        if (xlsdatafile != null && !xlsdatafile.getPath().isEmpty()){
            // very long string to show in screen so  gonna take only lastsegment
            // childFragment.setviewtextxlsdata(xlsdatafile.getPath());

            String onlydatafilename =  getFileNameFromUri(xlsdatafile); //.get

            childFragment.setviewtextxlsdata(onlydatafilename);
        }

        if (pdfmodelfile != null && !pdfmodelfile.getPath().isEmpty()){

            // very long string to show in screen so  gonna take only lastsegment
            //childFragment.setviewtextpdfmodel(pdfmodelfile.getPath());

            // pdfmodelfile.get
            String onlyfilename =  getFileNameFromUri(pdfmodelfile); //.getPath().
            childFragment.setviewtextpdfmodel(onlyfilename);
            Log.i("DEBUG", "  lastpathsegment " + onlyfilename);
           // childFragment.setviewtextpdfmodel(pdfmodelfile.getPath());
        }

        int intmode =  (openapp)?2:0+((openpdf)?1:0);
        childFragment.setspinneropenmode( intmode);


    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }


    public void mergePdfs(List<String> sourcePdfPaths, Uri outputPath) throws Exception {
        Document document = new Document();
        OutputStream os = getContentResolver().openOutputStream(outputPath);

        PdfCopy copy = new PdfCopy(document, os);
        document.open();

        for (String sourcePdfPath : sourcePdfPaths) {
            PdfReader reader = new PdfReader(sourcePdfPath);
            copy.addDocument(reader);
            reader.close();
        }

        document.close();
    }


    @Override
    public void onDataSent(boolean data) {
        // Handle the data received from the fragment
        Log.d("MyActivity", "Received data: " + data);
        flatten = data;

    }

    @Override
    public void onsetOpenmode(int mode) {
        sethandleresult(mode);
        saveparameter_conffile("openmergedfile", String.valueOf(mode));
    }


    void setdatafn(String[] fieldnames) {
        datafieldnames.clear();
        for (String fn : fieldnames) {
            datafieldnames.add(fn);
        }
        computecommonfields();

        for (String fn : datafieldnames)
            Log.i("DEBUG", "arquivo dados nome campos: " + fn);

    }

    void sethandleresult(int mode){
        Log.i("DEBUG", " method sethandleresult: " + mode);

        switch(mode){
            case 0:
                openapp = false;
                openpdf = false;
                break;
            case 1:
                openapp = false;
                openpdf = true;
                break;
            case 2:
                openapp = true;
                openpdf = true;
                break;
            case 3:


                break;
            default:
        }

    }


    void setmodelfn(Set<String> nmfn) {
        modelfieldnames = new HashSet<>(nmfn);
        computecommonfields();

        for (String fn : modelfieldnames)
            Log.i("DEBUG", "arquivo modelo nome campos: " + fn);

    }

    void computecommonfields() {
        commonfieldnames = new HashSet<>(datafieldnames);
        commonfieldnames.retainAll(modelfieldnames);


        for (String fn : commonfieldnames)
            Log.i("DEBUG", "em comum nome campos: " + fn);


    }

    //retorna os indices de fieladnames que estao  em commofieldnames
    int[] commoncols(String[] fieldnames) {
        //commonfieldnames;
        int countcommons = 0;
        for (int r = 0; r < fieldnames.length; r++)
            if (commonfieldnames.contains(fieldnames[r])) countcommons++;

        int[] ret = new int[countcommons];
        int ic = 0; //indice que vai percorrer ret
        for (int r = 0; r < fieldnames.length; r++)
            if (commonfieldnames.contains(fieldnames[r])) {
                ret[ic] = r;
                ic++;
            }


        return ret;
    }

    int[] commoncols2(Set<String>  commonfn, String[] fieldnames) {
        //commonfieldnames;
        int countcommons = 0;
        for (int r = 0; r < fieldnames.length; r++)
            if (commonfn.contains(fieldnames[r])) countcommons++;

        int[] ret = new int[countcommons];
        int ic = 0; //indice que vai percorrer ret
        for (int r = 0; r < fieldnames.length; r++)
            if (commonfn.contains(fieldnames[r])) {
                ret[ic] = r;
                ic++;
            }


        return ret;
    }

    int merge2() {

        //abertura arquivos de entrada e saida temporaria
        //para cada linha do arquivos de dados

        //abertura dos arquivos 2 de leitura  e 1 de saida

        List<String> temp_pdfs = new ArrayList<>();
        List<String> onlyfilenames = new ArrayList<>();

        try {


            String[] todos_nomes_campos;// =  new String[sdados.getRow(0).length];
            String[] valores_campos;// =  new String[sdados.getRow(0).length];
            int[] missing_fields;
            // Uri.
            if (xlsdatafile == null || pdfmodelfile == null) return -1;
            File tf = new File(xlsdatafile.getPath());
            Log.i("DEBUG", tf.getName() + " exist ?" + tf.exists() + "can read? " + tf.canRead() + "auth:" + xlsdatafile.getAuthority());
            //xlsdatafile.get
            // getContentResolver().open

            InputStream inputdata = getContentResolver().openInputStream(xlsdatafile);


            Workbook workbook = Workbook.getWorkbook(inputdata);// createWorkbook(fileOutputStream);
            //WritableSheet sheet = workbook.createSheet("Dados", 0);
            String[] sheets = workbook.getSheetNames();
            Sheet sdados = workbook.getSheet(0);
            Log.i("DEBUG", "nome da planilha: " + sdados.getName());

            int datalines = sdados.getRows() - 1;

            todos_nomes_campos = new String[sdados.getRow(0).length];
            for (int ind = 0; ind < sdados.getRow(0).length; ind++) {
                Cell c = sdados.getCell(ind, 0);
                todos_nomes_campos[ind] = c.getContents();
            }

            int[] indexes_common = commoncols2(commonfieldnames, todos_nomes_campos);


            valores_campos = new String[indexes_common.length];


            for (int x = 1; x <= datalines; x++) {


                InputStream input = getContentResolver().openInputStream(pdfmodelfile);// .OpenInputStream(data.getData);

                PdfReader reader = new PdfReader(input);


                String lastsegment = pdfmergedfile.getLastPathSegment();// Segments();
                lastsegment = "result.pdf";


                String lsnoext = lastsegment.substring(0, lastsegment.length() - 4);

                // String outfilename = lsnoext+"-"+x+ext;
                String outfilename_noext = lsnoext + "-" + x;
                DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
                DocumentFile newFile = AppDir.createFile("application/pdf", outfilename_noext);


                temp_pdfs.add(newFile.getUri().getPath());
                onlyfilenames.add(outfilename_noext);
                OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                PdfStamper stamper = new PdfStamper(reader, os);

                Log.i("DEBUG", "arquivos de intermediario: " + newFile.getUri().getPath());

//                FileOutputStream fos = new FileOutputStream(outputFile);
//                PdfStamper stamper = new PdfStamper(reader, fos);


                // 2. Obter os campos do formulário
                AcroFields form = stamper.getAcroFields();
                //merge


                for (int ind = 0; ind < indexes_common.length; ind++) {
                    Cell c = sdados.getCell(indexes_common[ind], x);
                    valores_campos[ind] = c.getContents();
                }


                //   Set<String> fieldNames = form.getFields().keySet();


                //     Iterator<String> e = fieldNames.iterator();


                //bug do itext versao  4.2 necessita chamra este setappearences  para o flattening funcionar
                //https://www.google.com/search?q=itext++4.2+setFormFlattening+turning+empty+document&client=firefox-b-d&sca_esv=d88ae73f6018b198&ei=_DEPaeqdGdPc1sQPnMmzmAY&ved=0ahUKEwjqi-fjweKQAxVTrpUCHZzkDGMQ4dUDCBA&uact=5&oq=itext++4.2+setFormFlattening+turning+empty+document&gs_lp=Egxnd3Mtd2l6LXNlcnAiM2l0ZXh0ICA0LjIgc2V0Rm9ybUZsYXR0ZW5pbmcgdHVybmluZyBlbXB0eSBkb2N1bWVudDIFEAAY7wUyBRAAGO8FMggQABiABBiiBDIIEAAYgAQYogQyBRAAGO8FSIIgUKUOWIkdcAJ4AJABAJgBzAGgAcUHqgEFMC40LjG4AQPIAQD4AQGYAgWgAsMFwgIIEAAYsAMY7wXCAgsQABiABBiwAxiiBMICChAhGKABGMMEGAqYAwCIBgGQBgWSBwUyLjAuM6AH8BayBwMyLTO4B4gFwgcFMy0zLjLIB00&sclient=gws-wiz-serp
                if (flatten) form.setGenerateAppearances(true);

                // 3. Preencher os campos
                //if (commonfieldnames.size > 0) {
                for (int r = 0; r < indexes_common.length; r++) {
                    // if (missing_fields[r] == 0)
                    form.setField(todos_nomes_campos[indexes_common[r]], valores_campos[r]);

                }
                //}


                // Opcional: remover a interatividade dos campos (PDF achatado)


                if (flatten) stamper.setFormFlattening(true);


                Log.i("DEBUG", "  fechando  ");


                // 4. Fechar o stamper
                stamper.close();
                reader.close();

            }

            Log.i("DEBUG", "  vai salvar intermediarios?" + (pdfintermfolder != null));


            if (pdfintermfolder != null) {
                //copiar do pdfs intermediarios pro diretorio desginado pelo usuario

                //  OutputStream(outputPath);
                for (int r = 0; r < temp_pdfs.size(); r++) {


                    String sourcePdfPath = temp_pdfs.get(r);
                    String fn = onlyfilenames.get(r);
                    File sourcefile = new File(sourcePdfPath);


                    File destfile = new File(pdfintermfolder.getPath(), fn);//   eUri(this, pdfmergedfile);
                    //  DocumentFile newFile = pickedDir.createFile("application/pdf", lsnoext+"-"+x+ext);

                    // getContentResolver().openInputStream( sourcePdfPath);

                    Uri.Builder ofb = new Uri.Builder();
                    ofb.path(pdfmergedfile.getPath());
                    ofb.appendPath(sourcePdfPath);
                    Uri outf = ofb.build();


                    Document document = new Document();


                    Uri.Builder turi = pdfintermfolder.buildUpon().appendPath(fn);
                    //  Uri    turi.build();

                    //    OutputStream os = getContentResolver().openOutputStream(,);
                    File outputFile = new File(pdfintermfolder.getPath(), fn);


                    //   File outputFile  = new File(pdfintermfolder.getPath(), fn);
                    //    outputFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    PdfCopy copy = new PdfCopy(document, fos);
                    document.open();

//                    copy.open();

                    PdfReader reader2 = new PdfReader(sourcePdfPath);

                    copy.addDocument(reader2);
                    reader2.close();


                    document.close();

                    // File destDir = new File(pdfmergedfile.getPath());

                    //FileCopier.copyFileUsingStreams(sourcefile, destfile );
                }

            }


            mergePdfs(temp_pdfs, pdfmergedfile);


            //deletando arquivos intermediario na pasta do aplicativo
            for (int r = 0; r < temp_pdfs.size(); r++) {


                String sourcePdfPath = temp_pdfs.get(r);
                File fileToDelete = new File(sourcePdfPath); // For internal storage
                // Or for cache: File fileToDelete = new File(context.getCacheDir(), "my_cache_file.tmp");
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                    Log.i("DEBUG", " deletou arquivo: " + sourcePdfPath);

                }
            }
            //reader2.close();
            Log.i("DEBUG", " depois de close ");

            //fechamento e salvamento do arquivo pdf
        } catch (Exception e) {
            e.printStackTrace();
        }


        return 0;
    }

    void initial_load_param() {
/*
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
        DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
        DocumentFile newFile = AppDir.createFile("application/vnd.ms-excel", "conf");
        if (!newFile.exists()) {
            Log.i("DEBUG", "criando arquivo de configuracao conf.xls");

            create_conffile();
        } else {

            List<UriPermission> lu = getContentResolver().getPersistedUriPermissions();
            for (int e = 0; e < lu.size(); e++) {
                Log.i("DEBUG", e+"persist path" + lu.get(e).getUri().getPath());
                Log.i("DEBUG", e+"persist read permission" + lu.get(e).isReadPermission());
            }

            //carregar variaveis
            //String xls = loadparameter_conffile("xlsdatafile");





            String valmode = loadparameter_conffile("openmergedfile");
            Log.i("DEBUG", "load open mode "+valmode);
            //if (!valmode.isEmpty() && valmode.i){
            try{
                int m = Integer.parseInt(valmode);
                if ( m >= 0 ) {
                    sethandleresult(m);
                } else {
                    Log.i("DEBUG", " erro parametro openmergefile nao e' numero positivo"+valmode);

                }
            } catch (NumberFormatException e){
                Log.i("DEBUG", " erro parametro openmergefile nao e' numero "+valmode);

            }




            File df = new File(getFilesDir(), "xlsdatafile");
            Log.i("DEBUG", df.getName() + " exist ?" + df.exists() + "can read? " + df.canRead());

            if (df.exists()){
                xlsdatafile = Uri.parse(loadurifromfile(Uri.fromFile(df)));
                read_header_datafile();
                //   childFragment.setviewtextxlsdata(xlsdatafile.getPath());

                // saveparameter_conffile("xlsdatafile", xlsdatafile.getPath());

                //getContentResolver().takePersistableUriPermission(xlsdatafile, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                //  read_header_datafile();
                Log.i("DEBUG", "auth:" + xlsdatafile.getAuthority());

                Log.i("DEBUG", " xls path:" + xlsdatafile.getPath());


            }

          //  String pdf = loadparameter_conffile("pdfmodelfile");
            /*
            if (!pdf.isEmpty()) {
                Uri.Builder ubp = new Uri.Builder();
                ubp.path(pdf);
                pdfmodelfile = ubp.build();
            }
            */
            File tf = new File(getFilesDir(), "pdfmodelfile");
            Log.i("DEBUG", tf.getName() + " exist ?" + tf.exists() + "can read? " + tf.canRead());

            if (tf.exists()){
//                getContentResolver().ope;
                pdfmodelfile = Uri.parse(loadurifromfile(Uri.fromFile(tf)));
                //  childFragment.setviewtextpdfmodel(pdfmodelfile.getPath());
                // saveparameter_conffile("xlsdatafile", xlsdatafile.getPath());
                //getContentResolver().takePersistableUriPermission(xlsdatafile, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                read_header_modelfile();


                Log.i("DEBUG", "auth:" + pdfmodelfile.getAuthority());

                Log.i("DEBUG", " pdf path:" + pdfmodelfile.getPath());


            }



        }
    }


    boolean create_conffile() {

        DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
        DocumentFile newFile = AppDir.createFile("application/vnd.ms-excel", "conf");
        try {
            //criar arquivo pela primeira vez
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(newFile.getUri(), "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            /*fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n").getBytes()); */
            // workbook.write(fileOutputStream);

            // Let the document provider know you're done by closing the stream.


            WritableWorkbook workbook = Workbook.createWorkbook(fileOutputStream);
            WritableSheet sheet = workbook.createSheet("Dados", 0);

            Label labelc = new Label(0, 0, "pdffiller");
            sheet.addCell(labelc);
            Label labelv = new Label(1, 0, "0.5");
            sheet.addCell(labelv);

            Label labelq = new Label (0, 3, "openmergedfile" );
            sheet.addCell(labelq);
            Label labelw = new Label (1, 3, "1" );
            sheet.addCell(labelw);


/*
            Label labelq = new Label (0, 1, paramname );
            sheet.addCell(labelq);
           Label labelw = new Label (1, 1, value );
            sheet.addCell(labelw);
*/


            workbook.write();
            workbook.close();
            fileOutputStream.close();
            pfd.close();

        } catch (java.io.FileNotFoundException e) {

        } catch (java.io.IOException e) {

        } catch (jxl.write.WriteException e) {

        }

        return true;


    }

    boolean saveparameter_conffile(String paramname, String value) {

        DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
        DocumentFile newFile = AppDir.createFile("application/vnd.ms-excel", "conf");
        Log.i("DEBUG", "saving conf file param:" + paramname + " value:" + value);

        File conffile = new File(getFilesDir(), "conf.xls");


        // if (newFile.exists()){
        //arquivo existe
        try {
            //criar arquivo pela primeira vez
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(newFile.getUri(), "rw");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            /*fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n").getBytes()); */
            // workbook.write(fileOutputStream);

            // Let the document provider know you're done by closing the stream.


            Workbook rwb = Workbook.getWorkbook(conffile);
            Sheet rsheet = rwb.getSheet(0);


            //  WritableWorkbook workbook = Workbook.

            //WritableSheet sheet = workbook.getSheet(0);

//                WritableSheet sheet = workbook.createSheet("Dados", 0);
            int rows = rsheet.getRows();
            Log.i("DEBUG", " rows: " + rows);

            int indl = 1;
            //  String cellstring = ;
            while (indl < rows && !rsheet.getCell(0, indl).getContents().equals(paramname)) {
                Log.i("DEBUG", "first column: " + rsheet.getCell(0, indl).getContents());

                //  Cell c = rsheet.getCell(0, indl);
                // cellstring = c.getContents();
                indl++;

            }
            Log.i("DEBUG", "indl" + indl);


            WritableWorkbook workbook;//  Workbook.createWorkbook() (rwb);
            workbook = Workbook.createWorkbook(conffile, rwb);

            WritableSheet ws = workbook.getSheet(0);
            Log.i("DEBUG", "name sheet:" + ws.getName());
            //escrita

            Label labelc = new Label(1, indl, value);
            Label ln = new Label(0, indl, paramname);
            ws.addCell(labelc);
            ws.addCell(ln);
                /*
                Label labelv = new Label (1, 0, "0.5" );
                sheet.addCell(labelv);

                Label labelq = new Label (0, 1, paramname );
                sheet.addCell(labelq);
                Label labelw = new Label (1, 1, value );
                sheet.addCell(labelw);*/

            rwb.close();

            workbook.write();
            workbook.close();
            fileOutputStream.close();
            pfd.close();

        } catch (java.io.FileNotFoundException e) {

        } catch (java.io.IOException e) {

        } catch (jxl.write.WriteException e) {

        } catch (BiffException e) {
            e.printStackTrace();
        }

        //    } else {


        return true;
    }

    String loadparameter_conffile(String paramname) {
        DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
        DocumentFile newFile = AppDir.createFile("application/vnd.ms-excel", "conf");
        String value = "";
        // if (newFile.exists()){
        //arquivo existe
        try {
            //criar arquivo pela primeira vez
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(newFile.getUri(), "r");
            FileInputStream fileInputStream =
                    new FileInputStream(pfd.getFileDescriptor());
            /*fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n").getBytes()); */
            // workbook.write(fileOutputStream);

            // Let the document provider know you're done by closing the stream.


            Workbook workbook = Workbook.getWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheet(0);
//                WritableSheet sheet = workbook.createSheet("Dados", 0);
            int rows = sheet.getRows();
            Log.i("DEBUG", "sheet name: " + sheet.getName() + " getrows" + rows);

            int indl = 1;
            // String cellstring =;
            while (indl < rows && !sheet.getCell(0, indl).getContents().equals(paramname)) {
                Log.i("DEBUG", "first column: " + sheet.getCell(0, indl).getContents());
                indl++;
                // Cell c = sheet.getCell(0, indl);
                //cellstring = c.getContents();

            }
            //if ()

//            Cell lb = sheet.getCell(0, indl);
            if (indl < rows) {
                Cell lc = sheet.getCell(1, indl);
                value = lc.getContents();
            }

                /*
                Label labelv = new Label (1, 0, "0.5" );
                sheet.addCell(labelv);

                Label labelq = new Label (0, 1, paramname );
                sheet.addCell(labelq);
                Label labelw = new Label (1, 1, value );
                sheet.addCell(labelw);*/


            //  workbook.write();
            workbook.close();
            fileInputStream.close();
            pfd.close();

        } catch (java.io.FileNotFoundException e) {

        } catch (java.io.IOException e) {

        } catch (BiffException e) {
            e.printStackTrace();
        }


        return value;
    }


    void read_header_datafile() {
        // getContentResolver().takePersistableUriPermission()
        try {
            //criar arquivo pela primeira vez
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(xlsdatafile, "r");
            FileInputStream fileInputStream =
                    new FileInputStream(pfd.getFileDescriptor());
        /*fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                "\n").getBytes()); */
            // workbook.write(fileOutputStream);

            // Let the document provider know you're done by closing the stream.


            Workbook workbook = Workbook.getWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheet(0);
//                WritableSheet sheet = workbook.createSheet("Dados", 0);


            Log.i("DEBUG", "nome da planilha: " + sheet.getName());


            String[] todos_nomes_campos = new String[sheet.getRow(0).length];
            for (int ind = 0; ind < sheet.getRow(0).length; ind++) {
                Cell c = sheet.getCell(ind, 0);
                todos_nomes_campos[ind] = c.getContents();
            }


            setdatafn(todos_nomes_campos);
            //int rows = sheet.getRows();

            workbook.close();
            fileInputStream.close();
            pfd.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (BiffException e) {
            e.printStackTrace();
        }

    }

    void read_header_modelfile() {
        if (pdfmodelfile != null && !pdfmodelfile.getPath().isEmpty()) {

            try {
                // getContentResolver().takePersistableUriPermission()

                InputStream input = null;// .OpenInputStream(data.getData);
                input = getContentResolver().openInputStream(pdfmodelfile);
                PdfReader reader = new PdfReader(input);

                AcroFields af = reader.getAcroFields();

                Set<String> fieldNames = af.getFields().keySet();

                setmodelfn(fieldNames);

                reader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {

            }
        }
    }

    boolean saveUritoFile(Uri sourceuri, File destfile) {
        InputStream is;
        FileOutputStream fos;
//        ObjectOutputStream oos;

        try {
            is = getContentResolver().openInputStream(sourceuri);
            fos = new FileOutputStream(destfile);

//            oos = new ObjectOutputStream(fos);

            if (is != null) {
              //  oos.writeObject(sourceuri);
               // oos.flush();

                byte[] buffer = new byte[4 * 1024];//(4*1024);
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
             //   oos.close();
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
        return true;
    }

    boolean saveUritoFile2(String uritostring, File destfile) {

        FileOutputStream fos;

        try {

            fos = new FileOutputStream(destfile);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeUTF(uritostring);
            // Convert the string to bytes using a specific character encoding (e.g., UTF-8)
          //  byte[] bytes = uritostring.getBytes(StandardCharsets.UTF_8);

            // Write the length of the string (optional, but useful for reading back)
            //dos.writeInt(bytes.length);

            // Write the bytes to the binary file
//            dos.write(bytes);

            dos.flush();
            dos.close();
            fos.close();

            } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    String loadurifromfile(Uri urifile) {
        InputStream is;
        String stringuri =null;
        try {
            is = getContentResolver().openInputStream(urifile);

            DataInputStream dis = new DataInputStream(is);
            stringuri = dis.readUTF();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringuri;
    }

    @Override
    public void onProgressUpdate(int progress) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
        FirstFragment childFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        childFragment.setprogressbar(progress);

        Log.i("DEBUG", " progress update "+progress);

    }

    @Override
    public void onComplete(String result) {




        if (openpdf){
            Intent i = new Intent()// Intent(Intent.ACTION_VIEW);

                    .setType("application/pdf")
                    .setAction(Intent.ACTION_VIEW)
                    .setData(pdfmergedfile)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                // Use createChooser to always show a list of apps if multiple are available

               if (openapp) {
                   startActivity(Intent.createChooser(i, "Open PDF with..."));
               } else {
                   startActivity(i);
               }
            } catch (ActivityNotFoundException e) {
                // Handle the case where no suitable PDF viewer is installed
                Toast.makeText(getApplicationContext(), "No application found to view PDF", Toast.LENGTH_SHORT).show();
            }

//                        intent.setFlags(intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

//                    registerForActivityResult()
            //    getActivity().startActivityForResult(Intent.createChooser(intent, "Select pdf model"), OPEN_PDFMODEL_TO_READ);



        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
        FirstFragment childFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        childFragment.setinvisibleprogressbar();


       // Thread.sleep(1000);


    }

    @Override
    public void onStartthread(int minv, int maxv) {
        // error trying set fragment views here
        //W/System.err: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        // so it will be commented and be before call the thread
        // inside  branch  if (requestCode == CREATEPDF_TO_WRITE && resultCode == RESULT_OK) {


            //super.onStartthread(min, maxv);
        /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main);
        FirstFragment childFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        childFragment.initprogressbar(minv, maxv);
        childFragment.setvisibleprogressbar();
        Log.i("DEBUG", "progress bar minv "+minv+ " maxv "+ maxv);
*/
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.i("DEBUG", " on item selecte "+ i + "  "+ l);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.i("DEBUG", " nothing selected");

    }


    class MergeService implements Callable<Integer> {
     /*
        private final Uri pdfmodel;
        private final Uri xlsdata;
        private final Uri pdfmerged;
*/
        private final ExecutorService pool;

        private final ProgressCallback callback;
  //      private final MergeServiceCallback callback;

/*

        public MergeService(Uri pdfmod, Uri xlsd, Uri pdfmer, int poolSize)
                throws IOException {
            pool = Executors.newFixedThreadPool(poolSize);


//            pdfmodel = pdfmod;
//            xlsdata = xlsd;
//            pdfmerged = pdfmer;


        }
*/
        public MergeService(int poolSize, ProgressCallback npcb)
                throws IOException {
            pool = Executors.newFixedThreadPool(poolSize);
            this.callback = npcb;

           /*
            pdfmodel = pdfmod;
            xlsdata = xlsd;
            pdfmerged = pdfmer;*/

        }


        @Override
        public Integer call() throws Exception {
            merge3();
            return 0;
        }

        int merge3(){
            int prog = 0;
            Log.i("DEBUG", "hellow start thread");




            //abertura arquivos de entrada e saida temporaria
            //para cada linha do arquivos de dados

            //abertura dos arquivos 2 de leitura  e 1 de saida

            List<String> temp_pdfs = new ArrayList<>();
            List<String> onlyfilenames = new ArrayList<>();

            try {


                String[] todos_nomes_campos;// =  new String[sdados.getRow(0).length];
                String[] valores_campos;// =  new String[sdados.getRow(0).length];
                int[] missing_fields;
                // Uri.
                if (xlsdatafile == null || pdfmodelfile == null) return -1;
                File tf = new File(xlsdatafile.getPath());
                Log.i("DEBUG", tf.getName() + " exist ?" + tf.exists() + "can read? " + tf.canRead() + "auth:" + xlsdatafile.getAuthority());
                //xlsdatafile.get
                // getContentResolver().open

                InputStream inputdata = getContentResolver().openInputStream(xlsdatafile);


                Workbook workbook = Workbook.getWorkbook(inputdata);// createWorkbook(fileOutputStream);
                //WritableSheet sheet = workbook.createSheet("Dados", 0);
                String[] sheets = workbook.getSheetNames();
                Sheet sdados = workbook.getSheet(0);
                Log.i("DEBUG", "nome da planilha: " + sdados.getName());

                int datalines = sdados.getRows() - 1;

                // each commonfiel times datalines  plus merge each temporary file plus remove each temp file
                int instructions_size =  commonfieldnames.size()*datalines + datalines*2;

                callback.onStartthread(0, instructions_size);

                todos_nomes_campos = new String[sdados.getRow(0).length];
                for (int ind = 0; ind < sdados.getRow(0).length; ind++) {
                    Cell c = sdados.getCell(ind, 0);
                    todos_nomes_campos[ind] = c.getContents();
                }

                int[] indexes_common = commoncols2(commonfieldnames, todos_nomes_campos);


                valores_campos = new String[indexes_common.length];


                for (int x = 1; x <= datalines; x++) {


                    InputStream input = getContentResolver().openInputStream(pdfmodelfile);// .OpenInputStream(data.getData);

                    PdfReader reader = new PdfReader(input);


                    String lastsegment = pdfmergedfile.getLastPathSegment();// Segments();
                    lastsegment = "result.pdf";


                    String lsnoext = lastsegment.substring(0, lastsegment.length() - 4);

                    // String outfilename = lsnoext+"-"+x+ext;
                    String outfilename_noext = lsnoext + "-" + x;
                    DocumentFile AppDir = DocumentFile.fromFile(getFilesDir());
                    DocumentFile newFile = AppDir.createFile("application/pdf", outfilename_noext);


                    temp_pdfs.add(newFile.getUri().getPath());
                    onlyfilenames.add(outfilename_noext);
                    OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                    PdfStamper stamper = new PdfStamper(reader, os);

                    Log.i("DEBUG", "arquivos de intermediario: " + newFile.getUri().getPath());

//                FileOutputStream fos = new FileOutputStream(outputFile);
//                PdfStamper stamper = new PdfStamper(reader, fos);


                    // 2. Obter os campos do formulário
                    AcroFields form = stamper.getAcroFields();
                    //merge


                    for (int ind = 0; ind < indexes_common.length; ind++) {
                        Cell c = sdados.getCell(indexes_common[ind], x);
                        valores_campos[ind] = c.getContents();
                    }


                    //   Set<String> fieldNames = form.getFields().keySet();


                    //     Iterator<String> e = fieldNames.iterator();


                    //bug do itext versao  4.2 necessita chamra este setappearences  para o flattening funcionar
                    //https://www.google.com/search?q=itext++4.2+setFormFlattening+turning+empty+document&client=firefox-b-d&sca_esv=d88ae73f6018b198&ei=_DEPaeqdGdPc1sQPnMmzmAY&ved=0ahUKEwjqi-fjweKQAxVTrpUCHZzkDGMQ4dUDCBA&uact=5&oq=itext++4.2+setFormFlattening+turning+empty+document&gs_lp=Egxnd3Mtd2l6LXNlcnAiM2l0ZXh0ICA0LjIgc2V0Rm9ybUZsYXR0ZW5pbmcgdHVybmluZyBlbXB0eSBkb2N1bWVudDIFEAAY7wUyBRAAGO8FMggQABiABBiiBDIIEAAYgAQYogQyBRAAGO8FSIIgUKUOWIkdcAJ4AJABAJgBzAGgAcUHqgEFMC40LjG4AQPIAQD4AQGYAgWgAsMFwgIIEAAYsAMY7wXCAgsQABiABBiwAxiiBMICChAhGKABGMMEGAqYAwCIBgGQBgWSBwUyLjAuM6AH8BayBwMyLTO4B4gFwgcFMy0zLjLIB00&sclient=gws-wiz-serp
                    if (flatten) form.setGenerateAppearances(true);

                    // 3. Preencher os campos
                    //if (commonfieldnames.size > 0) {
                    for (int r = 0; r < indexes_common.length; r++) {
                        // if (missing_fields[r] == 0)
                        form.setField(todos_nomes_campos[indexes_common[r]], valores_campos[r]);

                    }
                    //}


                    // Opcional: remover a interatividade dos campos (PDF achatado)


                    if (flatten) stamper.setFormFlattening(true);


                    Log.i("DEBUG", "  fechando  ");


                    // 4. Fechar o stamper
                    stamper.close();
                    reader.close();

                    prog +=  commonfieldnames.size();
                    callback.onProgressUpdate( prog);




                }

                Log.i("DEBUG", "  vai salvar intermediarios?" + (pdfintermfolder != null));


                if (pdfintermfolder != null) {
                    //copiar do pdfs intermediarios pro diretorio desginado pelo usuario

                    //  OutputStream(outputPath);
                    for (int r = 0; r < temp_pdfs.size(); r++) {


                        String sourcePdfPath = temp_pdfs.get(r);
                        String fn = onlyfilenames.get(r);
                        File sourcefile = new File(sourcePdfPath);


                        File destfile = new File(pdfintermfolder.getPath(), fn);//   eUri(this, pdfmergedfile);
                        //  DocumentFile newFile = pickedDir.createFile("application/pdf", lsnoext+"-"+x+ext);

                        // getContentResolver().openInputStream( sourcePdfPath);

                        Uri.Builder ofb = new Uri.Builder();
                        ofb.path(pdfmergedfile.getPath());
                        ofb.appendPath(sourcePdfPath);
                        Uri outf = ofb.build();


                        Document document = new Document();


                        Uri.Builder turi = pdfintermfolder.buildUpon().appendPath(fn);
                        //  Uri    turi.build();

                        //    OutputStream os = getContentResolver().openOutputStream(,);
                        File outputFile = new File(pdfintermfolder.getPath(), fn);


                        //   File outputFile  = new File(pdfintermfolder.getPath(), fn);
                        //    outputFile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(outputFile);

                        PdfCopy copy = new PdfCopy(document, fos);
                        document.open();

//                    copy.open();

                        PdfReader reader2 = new PdfReader(sourcePdfPath);

                        copy.addDocument(reader2);
                        reader2.close();


                        document.close();

                        // File destDir = new File(pdfmergedfile.getPath());

                        //FileCopier.copyFileUsingStreams(sourcefile, destfile );

                    }

                }


                //mergePdfs(temp_pdfs, pdfmergedfile);
                {
                    List<String> sourcePdfPaths = new ArrayList<>(temp_pdfs);
                    Uri outputPath = pdfmergedfile;

                    Document document = new Document();
                    OutputStream os = getContentResolver().openOutputStream(outputPath);

                    PdfCopy copy = new PdfCopy(document, os);
                    document.open();

                    for (String sourcePdfPath : sourcePdfPaths) {
                        PdfReader reader = new PdfReader(sourcePdfPath);
                        copy.addDocument(reader);
                        reader.close();
                        prog++;
                        callback.onProgressUpdate( prog);

                    }

                    document.close();

                }
               // prog += datalines;
              //  callback.onProgressUpdate( prog);



                //deletando arquivos intermediario na pasta do aplicativo
                for (int r = 0; r < temp_pdfs.size(); r++) {


                    String sourcePdfPath = temp_pdfs.get(r);
                    File fileToDelete = new File(sourcePdfPath); // For internal storage
                    // Or for cache: File fileToDelete = new File(context.getCacheDir(), "my_cache_file.tmp");
                    if (fileToDelete.exists()) {
                        fileToDelete.delete();
                        Log.i("DEBUG", " deletou arquivo: " + sourcePdfPath);

                    }
                    prog ++;
                    callback.onProgressUpdate(prog);

                }

//                Thread.sleep(1000);

                //reader2.close();
                Log.i("DEBUG", " depois de close ");
                callback.onComplete("success");



                //fechamento e salvamento do arquivo pdf
            } catch (Exception e) {
                e.printStackTrace();
            }


            return 0;


        }


    }

    public String getFileNameFromUri( Uri contentUri) {
        String fileName = null;
       // ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }



    private boolean writeFile(Uri uri) {

        try {

            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            /*fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n").getBytes()); */
            // workbook.write(fileOutputStream);

            // Let the document provider know you're done by closing the stream.

            WritableWorkbook workbook = Workbook.createWorkbook(fileOutputStream);
            WritableSheet sheet = workbook.createSheet("fields", 0);


          //  modelfieldnames


           // String[] columns = new String[]{" * "};
            //  Cursor curCSV = handler.DisplayDataSelect(colnames, tablename, filter, "", sort);
          //  Cursor curCSV = handler.Query(current_query);
            // Cursor curCSV2 = DisplayDataSelect(columns, tablename, filt, group, sort);
            int ncol = modelfieldnames.size();
            Log.i("DEBUG", "exportacao");

            // handler.table_columns(fake_tablename);
            int e = 0;
            for (String colname:modelfieldnames){
                Label labelc = new Label (e, 0, colname);
                sheet.addCell(labelc);
                e++;
                Log.i("DEBUG", "campos: "+colname);

            }

            workbook.write();
            workbook.close();
            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

        return true;
    }
}