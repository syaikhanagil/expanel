package net.syaikhanagil.expanel.sample;

import android.os.*;
import androidx.appcompat.app.AppCompatActivity;
import net.syaikhanagil.expanel.ExPanel;

public class MainActivity extends AppCompatActivity 
{
	ExPanel mExPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		mExPanel = (ExPanel) findViewById(R.id.idExPanel);
		
		/* Add Listener
		mExPanel.setExPanelListener(new ExPanel.ExListener(){
				@Override
				public void onExPanelChanged(ExPanel expanel, boolean isExpand)
				{
					// TODO: Implement this method
				}
			});
		*/
    }
}
