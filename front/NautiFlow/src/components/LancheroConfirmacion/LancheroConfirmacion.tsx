import HeaderConfirmacion from "./HeaderConfirmacion/HeaderConfirmacion"
import BodyConfirmation from "./BodyConfirmacion/BodyConfirmation"
type Props = { finalizar: () => void; };
export default function LancheroConfirmacion({finalizar}:Props){
    return(
        <div className=" flex flex-col  h-svh p-4  md:py-8 md:px-15 md:gap-15 " >
            <HeaderConfirmacion/>
            <div className="py-3 md:h-screen flex md:justify-center">
               <BodyConfirmation finalizar={finalizar}/>
            </div>
        </div>
    )



}