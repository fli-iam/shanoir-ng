/*
** DcmCSAHeader.hh
** Login : <nwiestda@pallium.irisa.fr>
** Started on  Tue Apr 11 17:02:11 2006 Nicolas Wiest-Daessle
** $Id$
** 
** Copyright (C) 2006 Nicolas Wiest-Daessle
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
** 
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
** 
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

#ifndef   	DCMCSAHEADER_HH_
# define   	DCMCSAHEADER_HH_

  struct CSARoot
  {
    char c[4], pad[4];
    int n;
    unsigned char xx[4];// ASSERT == 'M\0\0\0'
  };

  struct CSANode
  {
    char name[64];
    int vm;
    char vr[4];
    int syngodt;
    int nitems;
    unsigned char xx[4];// ASSERT ==  'M\0\0\0'    
  };
  struct CSAItem
  {
    int xx[4];
    //char len 
    char val[0];
  };


  struct CSAHeader : public DcmElement
  {
    CSAHeader(DcmElement& elt): DcmElement(elt)
    { dump(); }
    virtual DcmObject* clone() const { return NULL; }
    virtual DcmEVR ident() const { return DcmEVR(); }
    virtual void print(std::ostream&, size_t, int, const char*, size_t*) {}
    virtual long unsigned int getVM() { return 0; }
    virtual OFCondition verify(bool) { return OFCondition();}
    std::vector<float> grad;
    float b0;
    std::map<std::string, std::list<CSAItem*> > map2;

    void dump()
    {
      char* buf = (char*)getValue();
      std::list<CSANode*> Nodes;
      std::map<CSANode*, std::list<CSAItem*> > map;

      int gap = 0;
      CSARoot *root = (CSARoot*)(buf); 
      gap += sizeof(CSARoot);
      if (root->n > 128 || root->n < 0)  { std::cerr << "Junk CSA Header Node!" << std::endl; return; }
      //std::cout << root->xx[0] << ' '  << root->xx[1] << ' '  << root->xx[2] << ' '  << root->xx[3] << std::endl;
      assert(root->xx[0] == 'M');
      //      std::cout << "Nodes " << root->n << std::endl;
      for (int i = 0; i < root->n; ++i)
	{
	  CSANode *n = (CSANode*)(&buf[gap]); 
	  gap += sizeof(CSANode);
	  Nodes.push_back(n);
	  if (!(n->xx[0] == 77 || n->xx[0] == 205))
	    std::cerr << "Error XX value " << (int)n->xx[0] << std::endl;
	  //std::cout << "Node Name :" << n->name  << " " << n->nitems << std::endl; 

	  for (int j = 0; j < n->nitems; ++j)
	    {
	      CSAItem* item = (CSAItem*)(&buf[gap]);
	      assert(item->xx[2] == 77 || item->xx[2] == 205);
	      //	      std::cout << "Item Size " <<
	      //	      (int)item->xx[0] << ' ' <<
	      //	      (int)item->xx[1] << ' ' <<
	      //	      (int)item->xx[3] << std::endl;
	      //	      for (int l = 0; l <  item->xx[1] + (4 - item->xx[1] % 4) % 4; ++l)
	      //		{
	      //		  std::cout << item->val[l];
	      //		}
	      //	      std::cout << std::endl;
	      gap += 4*sizeof(int) + item->xx[1] + (4 - item->xx[1] % 4) % 4;
	      map[n].push_back(item);
	      map2[n->name].push_back(item);
	    }	  
	  
	}
      //	std::cout << map2["UsedChannelMask"].front()->val[i];
      //std::cout << std::endl;
    }


    bool hasGradients()
    {
      return (map2.count("DiffusionGradientDirection") != 0);
    }
  

    std::vector<float> gradients()
    {
      if (map2.count("DiffusionGradientDirection") != 0)
	{
	  std::list<CSAItem* >::iterator it = map2["DiffusionGradientDirection"].begin(); 
	  grad.push_back(atof((*it)->val)); ++it;
	  grad.push_back(atof((*it)->val)); ++it;
	  grad.push_back(atof((*it)->val)); ++it;
	}

      return grad;
    }

    float BValue()
    {
      b0 = atof((char*)&(map2["B_value"].front()->val));
      //return atof(map2["B_value"].front()->val);
      return b0;
    }
    
  };


#endif	    /* !DCMCSAHEADER_HH_ */
