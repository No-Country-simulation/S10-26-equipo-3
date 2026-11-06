import Button from '../../Button/Button'
type Props = { finalizar: () => void; };
export default function BodyConfirmation({finalizar}:Props){

    return(

    <div className='flex flex-col  '>
        <div className="flex flex-row justify-center ">
            <svg width="88" height="88" viewBox="0 0 88 88" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M31.1667 43.6667L39.5 52L58.25 33.25M24.224 9.5779C27.5733 9.31062 30.753 7.99357 33.3103 5.81425C39.278 0.728584 48.0553 0.728584 54.023 5.81425C56.5804 7.99357 59.76 9.31062 63.1093 9.5779C70.9252 10.2016 77.1317 16.4081 77.7554 24.224C78.0227 27.5733 79.3398 30.753 81.5191 33.3103C86.6048 39.278 86.6048 48.0553 81.5191 54.023C79.3398 56.5804 78.0227 59.76 77.7554 63.1093C77.1317 70.9252 70.9252 77.1317 63.1093 77.7554C59.76 78.0227 56.5804 79.3398 54.023 81.5191C48.0553 86.6048 39.278 86.6048 33.3103 81.5191C30.753 79.3398 27.5733 78.0227 24.224 77.7554C16.4081 77.1317 10.2016 70.9252 9.5779 63.1093C9.31062 59.76 7.99357 56.5804 5.81425 54.023C0.728584 48.0553 0.728584 39.278 5.81425 33.3103C7.99357 30.753 9.31062 27.5733 9.5779 24.224C10.2016 16.4081 16.4081 10.2016 24.224 9.5779Z" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" stroke="#205362"/>
            </svg>
           

        </div>
        <div className='flex flex-col gap-8 md:gap-12 md:justify-center md:items-center'>
            <div className="flex  flex-col text-center py-8 gap-3">
                <div className="w-full h-7">
                    <h1 className="text-subtitle-md-size leading-subtitle-md-line-height font-heading-lg">Solicitud enviada con éxito!</h1>
                </div>

                <div>
                    <p className="leading-body-md-regular-line-height text-body-md-regular-size font-body-md-regular text-text-primary">Recibimos tus datos y documentos correctamente.</p>
                </div>
            </div>
            <div className="flex flex-col gap-4">
                <div className="flex flex-row gap-1">
                    <span>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M2 7L10.1649 12.7154C10.8261 13.1783 11.1567 13.4097 11.5163 13.4993C11.8339 13.5785 12.1661 13.5785 12.4837 13.4993C12.8433 13.4097 13.1739 13.1783 13.8351 12.7154L22 7M6.8 20H17.2C18.8802 20 19.7202 20 20.362 19.673C20.9265 19.3854 21.3854 18.9265 21.673 18.362C22 17.7202 22 16.8802 22 15.2V8.8C22 7.11984 22 6.27976 21.673 5.63803C21.3854 5.07354 20.9265 4.6146 20.362 4.32698C19.7202 4 18.8802 4 17.2 4H6.8C5.11984 4 4.27976 4 3.63803 4.32698C3.07354 4.6146 2.6146 5.07354 2.32698 5.63803C2 6.27976 2 7.11984 2 8.8V15.2C2 16.8802 2 17.7202 2.32698 18.362C2.6146 18.9265 3.07354 19.3854 3.63803 19.673C4.27976 20 5.11984 20 6.8 20Z" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" stroke="#2F3133"/>
                        </svg>
                    </span>
                    <div>
                        <h2 className="text-text-secondary text-body-sm-semibold-size leading-body-sm-semibold-line-height font-body-sm-semibold" >Revisa tu correo electrónico</h2>
                        <p  className="text-text-secondary text-body-sm-regular-size leading-body-sm-regular-line-height font-body-sm-regular">Te enviamos un correo a la dirrección que registraste para confirmar que recibimos tu solicitud.</p>
                    </div>
                </div>

                <div className="flex flex-row gap-1">
                    <span>
                        <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10.75 4.75V10.75L14.75 12.75M20.75 10.75C20.75 16.2728 16.2728 20.75 10.75 20.75C5.22715 20.75 0.75 16.2728 0.75 10.75C0.75 5.22715 5.22715 0.75 10.75 0.75C16.2728 0.75 20.75 5.22715 20.75 10.75Z" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" stroke="#2F3133"/>
                        </svg>
                    </span>
                    <div className="flex flex-col gap-1">
                        <h2  className="text-text-secondary text-body-sm-semibold-size leading-body-sm-semibold-line-height font-body-sm-semibold" >Qué sigue?</h2>
                        <p  className="text-text-secondary text-body-sm-regular-size leading-body-sm-regular-line-height font-body-sm-regular">Revisaremos tu solicitud y te notificaremos por correo electrónico cuando sea aprobada o rechazada, mantente atento a tu correo.</p>
                    </div>
                </div>
                        
            </div>
            <div className='flex flex-col gap-4'>
                <div className="text-center px-3 pt-10">       
                        <p className=" text-text-info text-caption-semibold-size leading-caption-semibold-line-height font-caption-semibold">Si no encuentras el correo de confirmación, revisa tu carpeta de spam o correo no deseado.</p>
                </div>
                <Button type='button' texto='Volver a inicio' onclick={finalizar}/>
        </div>
        </div>
    
    
    </div>
    )
}