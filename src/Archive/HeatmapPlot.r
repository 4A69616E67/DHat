#=====================================
library(getopt)
argument=matrix(c(
  'input','i',1,"character","dense matrix file",
  'chr1','1',1,"character","chromosome and start site, such as chr1:0",
  'chr2','2',2,"character","chromosome and start site, if not set, the value will same as \"chr1\"",
  'res','r',1,"integer","matrix resolution",
  'quantile','q',2,"double","the value of quantile (default 0.98)",
  'png_format','png',0,"logical","output png file",
  'help','h',0,"logical","print help"
),byrow = T,ncol = 5)
opt=getopt(argument)
if(!is.null(opt$help)||is.null(opt$input)|| is.null(opt$chr1)|| is.null(opt$res)){
  cat(paste(getopt(argument,usage = T),"\n"))
  q()
}
#=====================================
library(ggplot2)
library(reshape2)
filename=opt$input
chrs=unlist(strsplit(opt$chr1,split = ":"))
col_name=chrs[1];col_start_value=as.integer(chrs[2])
row_name=col_name;row_start_value=col_start_value
if(!is.null(opt$chr2)){
  chrs=unlist(strsplit(opt$chr2,split = ":"))
  col_name=chrs[1];col_start_value=as.integer(chrs[2])
}
resolution=opt$res
quantile_value=0.98
if(!is.null(opt$quantile)){
  quantile_value=opt$quantile
}
#=========================================
data<-read.table(filename,sep = "\t")
names(data)<-c(1:ncol(data))
data$id<-row.names(data)
data_x<-melt(data,id.vars = "id")
rm(data)
names(data_x)<-c("Var1","Var2","value")
data_x<-data_x[which(!is.na(data_x$value)),]
quan<-as.data.frame(quantile(data_x$value,quantile_value))[1,1]
data_x[data_x$value>quan,]$value<-quan
data_x$Var1<-as.numeric(data_x$Var1)
data_x$Var2<-as.numeric(data_x$Var2)
rect<-data.frame(xmin=min(data_x$Var1),xmax=max(data_x$Var1),
                 ymin=min(data_x$Var2),ymax=max(data_x$Var2))
x_breaks=c(0:floor(rect$xmax/100))*100
x_lable=(c(0:floor(rect$xmax/100))*100*resolution+col_start_value)/1000000
# x_lable[length(x_lable)]<-paste(x_lable[length(x_lable)],"/(M)")
y_breaks=c(0:floor(rect$ymax/100))*100
y_lable=(c(0:floor(rect$ymax/100))*100*resolution+row_start_value)/1000000
# y_lable[length(y_lable)]<-paste(y_lable[length(y_lable)],"/(M)")
if(!is.null(opt$png_format)){
  png(paste(filename,"png",sep = "."),width=650*3,height=3*600,res=72*3)
}else{
  pdf(paste(filename,"pdf",sep = "."),width = 7.7)
}
(ggplot(data_x)
  +geom_tile(aes(x=Var1,y=Var2,fill=value))
  +scale_fill_gradient(low = "#FFF5F0",high = "dark red")
  +geom_rect(data=rect,aes(xmin=xmin-0.5,xmax=xmax+0.5,ymin=ymin-0.5,ymax=ymax+0.5),fill=NA,color="black")
  +geom_segment(data=data.frame(value= y_breaks),aes(x=10,xend=0.5,y=value+0.5,yend=value+0.5))
  +geom_segment(data=data.frame(value= x_breaks),aes(y=10,yend=0.5,x=value+0.5,xend=value+0.5))
  +theme_minimal()
  +xlab(paste(col_name,"(M)") )+ylab(paste(row_name,"(M)"))
  +scale_x_continuous(breaks = x_breaks,labels = x_lable)
  +scale_y_continuous(breaks = y_breaks,labels = y_lable)
  +theme(axis.text.x = element_text(vjust = 8))
  +theme(axis.text.y = element_text(angle = 90,hjust = 0.5, vjust = -8))
  +theme(axis.text=element_text(size=14))
  +theme(axis.title.x = element_text(vjust = 8))
  +theme(axis.title.y = element_text(vjust = -8))
  +theme(axis.title = element_text(size = 20))
  +theme(panel.grid = element_blank())
)
dev.off()
