library(dplyr)
library(ggplot2)

options(repr.plot.width=5, repr.plot.height=4)

results = read.csv('build/eval-results2.csv')
head(results)

static_results = results #%>% filter(is.na(NNbrs))
head(static_results)

temp = results %>% filter(NNbrs==100)
temp1 = results %>% filter(Algorithm=="PersMean")
result <- rbind(temp,temp1)
ggplot(result) +
  aes(x=Algorithm, y=RMSE.ByRating) +
  geom_boxplot()

popular_results = results %>% filter(Algorithm=="Popular")
ggplot(popular_results) +
  aes(x=Algorithm, y=TopN.nDCG) +
  geom_boxplot()

ggplot(static_results) +
  aes(x=Algorithm, y=TopN.nDCG) +
  geom_boxplot()


ggplot(static_results) +
  aes(x=Algorithm, y=TopN.TagEntropy) +
  geom_boxplot()


user_user_cosine_results = results %>% filter(Algorithm=="UserUserCosine")
ggplot(user_user_cosine_results) +
  aes(group=NNbrs ,x=NNbrs, y=TopN.TagEntropy) +
  geom_boxplot()

pers_mean_results = results %>% filter(Algorithm=="PersMean")
ggplot(pers_mean_results) +
  aes(group=NNbrs ,x=NNbrs, y=TopN.TagEntropy) +
  geom_boxplot()

user_user_results = results %>% filter(Algorithm=="UserUser")
ggplot(user_user_results) +
  aes(group=NNbrs ,x=NNbrs, y=TopN.nDCG) +
  geom_boxplot()

item_item_results = results %>% filter(Algorithm=="ItemItem")
ggplot(item_item_results) +
  aes(group=NNbrs ,x=NNbrs, y=TopN.nDCG) +
  geom_boxplot()


lucene_results = results %>% filter(Algorithm=="Lucene")
ggplot(lucene_results) +
  aes(group=NNbrs ,x=NNbrs, y=TopN.nDCG) +
  geom_boxplot()


