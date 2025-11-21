(function(){
  function loadImage(url){
    return new Promise(function(resolve, reject){
      var img=new Image();
      img.crossOrigin='anonymous';
      img.onload=function(){ resolve(img); };
      img.onerror=function(e){ reject(e); };
      img.src=url;
    });
  }
  async function compressImageToUnder(file, maxBytes){
    var limit=maxBytes||5120;
    var url=URL.createObjectURL(file);
    try{
      var img=await loadImage(url);
      var w=img.naturalWidth||img.width;
      var h=img.naturalHeight||img.height;
      var sizes=[1024,800,600,400,300,200,150,120,96];
      var qualities=[0.7,0.6,0.5,0.4,0.3,0.25,0.2,0.15,0.1];
      var canvas=document.createElement('canvas');
      var ctx=canvas.getContext('2d');
      var blob=null;
      for(var i=0;i<sizes.length;i++){
        var s=sizes[i];
        var base=Math.max(w,h);
        var ratio=Math.min(1, s/base);
        var tw=Math.max(1, Math.floor(w*ratio));
        var th=Math.max(1, Math.floor(h*ratio));
        canvas.width=tw; canvas.height=th;
        ctx.clearRect(0,0,tw,th);
        ctx.drawImage(img,0,0,tw,th);
        for(var j=0;j<qualities.length;j++){
          var q=qualities[j];
          blob=await new Promise(function(r){ canvas.toBlob(r,'image/jpeg',q); });
          if(blob && blob.size<=limit) return blob;
        }
      }
      return blob||file;
    } finally {
      URL.revokeObjectURL(url);
    }
  }
  window.compressImageToUnder=compressImageToUnder;
})();