%% ***********************
%
clear all 
clc

datos = load('time_alpha_score.csv');

datos_media = datos(:,3);

step = 500;

for index=1:step:length(datos_media)+1
    
   if index+step > length(datos_media)
       media = mean(datos_media(index:length(datos_media), 1));
       datos_media(index:length(datos_media), 1) = media;
   else
       media = mean(datos_media(index:index+step, 1));
       datos_media(index:index+step, 1) = media;
   end
   

end

plot(datos(:,1), datos(:,2), 'r')
hold on;
plot(datos(:,1),mat2gray(datos_media), 'b')
legend({'Alpha', 'Score'})
title('Time - Alpha - Score')
xlabel('Ticks')

%% ********************
%
clear all 
clc

datos = load('VisitedStates.csv');

sumatorio = sum(datos,2);

plot(1:200, sumatorio);



