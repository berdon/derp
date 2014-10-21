DataBinding for Android
====
Derp is a small, reflection (*for now*) driven library that makes it easy to bind data to XML layouts. The big features at this point are:
* Efficiently serves list data via `Adapters` (yes, with `View` recycling)
* Supports primitive event driven updates via `DataBinding<?>`

What's left?
* Adding support for complex objects implementing a `DataBinding<?>` interface to get free updates
* `CursorAdapter` support
* A better README

**Where doth one get derp?**
```gradle
compile 'com.jajuka:derp:1.0.0'
```
## What can derp do?
To dive into a sample project using Derp to create a TODO list app replete with lists and item view `onclick` handling goodness, checkout [derp-todo](http://berdon.github.com/derp-todo).

<img src="http://berdon.github.io/derp/assets/derp-todo-01.png" width=250>

### To wet your whistle
For starters, stuff. List stuff.

**activity_main.xml**
```xml
<ListView xmlns:android="http://schemas.android.com/apk/res/android"
              android:id="@+id/listview"
              android:layout_width="match_parent"
              android:layout_height="match_parent" />
```

**item_text.xml**
```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
              android:id="@+id/text"
              android:tag="setText:"
              android:layout_width="match_parent"
              android:layout_height="wrap_content" />
```

**MainActivity.java**
```java
public class MainActivity extends Activity {
    // Create a DataBinding for a list of Models
    @Bind(value = R.id.listview, repeat = true, layoutId = R.layout.item_text)
    private List<String> mNames = new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create some "names"
        for (int i = 0; i < 1000; i++) {
            mNames.add(String.valueOf(i));
        }

        // Inject our views
        ButterKnife.inject(this);

        // Bind our data
        Derp.bind(this);
    }
}
```

## So, what'd that do?
It created an `Adapter` behind the scenes to feed the names *efficiently* into the `ListView`. Yeah, *efficiently*, we're reusing `View`s here.

## That seems fun and awesome - what else?
Derp supports simple stuff too.

**activity_main.xml**
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:orientation="vertical" >
    <TextView
        android:id="@+id/text"
        android:tag="setText:"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
</LinearLayout>
```

**MainActivity.java**
```java
public class MainActivity extends Activity {
    // Create a DataBinding for a list of Models
    @Bind(R.id.text)
    private String mHelloText = "Herp Derp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inject our views
        ButterKnife.inject(this);

        // Bind our data
        Derp.bind(this);
    }
}
```

## Oh, ok...that's cool
No, wait, it is. `DataBinding<?>`s let us modify primitives and see the changes for free.

**MainActivity.java**
```java
public class MainActivity extends Activity {
    // Create a DataBinding for a list of Models
    @Bind(R.id.text)
    private DataBinding<String> mHelloText = new DataBinding<String>("Herp Derp");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inject our views
        ButterKnife.inject(this);

        // Bind our data
        Derp.bind(this);

        // Let's update the text later
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mHelloText.set("See, this is cool!");
            }
        }, 1000);
    }
}
```

What about complex datatypes?
Those are fine too.

**Model.java**
```java
public class Model {
    public String Name = "Your name here";
}
```

**activity_main.xml**
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:orientation="vertical" >
    <TextView
        android:id="@+id/text"
        android:tag="setText:Name"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
</LinearLayout>
```

Note: *Name corresponds to the Name field in the bound property (the Model object).* You can path into any hierarchy, including arrays. (Ie. setText:Some.Object[2].Here)

**MainActivity.java**
```java
public class MainActivity extends Activity {
    // Create a DataBinding for a list of Models
    @Bind(R.id.text)
    private DataBinding<Model> mModel = new DataBinding<Model>(new Model());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inject our views
        ButterKnife.inject(this);

        // Bind our data
        Derp.bind(this);

        // Let's update the text later
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mModel.get().Name = "A different name";

                // For now, we need to tell the DataBinding about the change
                // TODO: Update complex model to handle reporting this itself
                mModel.update();
            }
        }, 1000);
    }
}
```
