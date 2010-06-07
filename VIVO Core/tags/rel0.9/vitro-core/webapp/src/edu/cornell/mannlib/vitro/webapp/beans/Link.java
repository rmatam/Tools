package edu.cornell.mannlib.vitro.webapp.beans;

/*
Copyright (c) 2010, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


// TODO bjl23 this is the link method, nothing to do here, just drawing your attention to it.

public class Link extends BaseResourceBean {
    private String url = null;
    private String anchor = null;
    private String entityURI = null;
    private String typeURI = null;
    private String displayRank = "-1";
    private ObjectPropertyStatement objectPropertyStatement = null;

    public String getAnchor() {
        return anchor;
    }
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }
    public String getEntityURI() {
        return entityURI;
    }
    public void setEntityURI(String entityURI) {
        this.entityURI = entityURI;
    }
    public String getTypeURI() {
        return typeURI;
    }
    public void setTypeURI(String typeURI) {
        this.typeURI = typeURI;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDisplayRank() {
        return displayRank;
    }
    
    public void setDisplayRank(int rank) {
        this.displayRank = String.valueOf(rank);
    }
    public void setDisplayRank(String rank) {
        this.displayRank = rank;
    /*  try {
            this.displayRank = Integer.parseInt(rank);
        } catch (NumberFormatException ex) {
            this.displayRank = 10;
        } */
    }

    public ObjectPropertyStatement getObjectPropertyStatement() {
        return objectPropertyStatement;
    }
    public void setObjectPropertyStatement(ObjectPropertyStatement op) {
        this.objectPropertyStatement = op;
    }
}