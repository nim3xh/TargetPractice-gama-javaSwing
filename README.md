CREATE Satatic APP HOST

01 create Repor

02 Shell

	$ ls
	$ mkdir <folder name>
	$ cd ictb
  ictb	$ git clone https://github.com/Azure-Samples/html-docs-hello-world.git 
	
	-- go to this clone web folder

	$ ls
	$ cd html-docs-hello-world			lkwebapp01
	$ az webapp up --location westeurope --name <app_name> --html

	-- link copy or click pase app open to web browser

03 Eddit Code

	html-docs-hello-world $ ls 
			      $ nano index.html
			
			      Save (Ctrl + O ---> Ctrl + X) 

			     $ az webapp up --location westeurope --name lkwebapp01 --html
						      
			
04 Go to Resource Group

	i Go search bar
	ii go to your shell copy : lahirupresath262_rg_5624
	iii search bar paste : lahirupresath262_rg_5624
	
05 Delete 
	$ az group delete --name appsvc_rg_Windows_westeurope
